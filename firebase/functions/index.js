const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const { setGlobalOptions } = require('firebase-functions/v2');
const admin = require('firebase-admin');

admin.initializeApp();
setGlobalOptions({ maxInstances: 10, region: 'southamerica-east1' });

function periodKeys(timestamp) {
  const date = new Date(timestamp);
  const month = `${date.getUTCFullYear()}-${String(date.getUTCMonth() + 1).padStart(2, '0')}`;
  const firstDay = new Date(Date.UTC(date.getUTCFullYear(), 0, 1));
  const week = Math.ceil((((date - firstDay) / 86400000) + firstDay.getUTCDay() + 1) / 7);
  return [`week-${date.getUTCFullYear()}-W${String(week).padStart(2, '0')}`, `month-${month}`, 'all'];
}

exports.validateCheckIn = onDocumentCreated('checkins/{checkInId}', async (event) => {
  const snapshot = event.data;
  if (!snapshot) return;
  const checkIn = snapshot.data();
  if (checkIn.status !== 'PENDING_SYNC') return;

  const db = admin.firestore();
  const checkInRef = snapshot.ref;
  const missionRef = db.doc(`missions/${checkIn.missionId}`);
  const mission = await missionRef.get();
  if (!mission.exists || mission.data().enabled !== true) {
    await checkInRef.update({ status: 'REJECTED', rejectionReason: 'MISSION_NOT_FOUND' });
    return;
  }
  const officialXp = Number(mission.data().xpReward || 0);
  const completedAt = Number(checkIn.completedAt || 0);
  const startedAt = Number(checkIn.startedAt || completedAt);
  const durationSeconds = Number(checkIn.durationSeconds || 0);
  if (completedAt < startedAt || completedAt - startedAt > 24 * 60 * 60 * 1000 || Math.abs(durationSeconds - Math.floor((completedAt - startedAt) / 1000)) > 2) {
    await checkInRef.update({ status: 'REJECTED', rejectionReason: 'INVALID_DURATION' });
    return;
  }

  const userId = checkIn.userId;
  const memberships = await db.collectionGroup('members').where('userId', '==', userId).get();
  const batch = db.batch();
  batch.update(checkInRef, { status: 'VALIDATED', xpEarned: officialXp, validatedAt: admin.firestore.FieldValue.serverTimestamp() });
  const statsRef = db.doc(`userStats/${userId}`);
  batch.set(statsRef, {
    userId,
    totalXp: admin.firestore.FieldValue.increment(officialXp),
    totalCheckIns: admin.firestore.FieldValue.increment(1),
    activeSeconds: admin.firestore.FieldValue.increment(Math.max(0, durationSeconds)),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  }, { merge: true });
  memberships.forEach((member) => {
    const tavernRef = member.ref.parent.parent;
    if (!tavernRef) return;
    const feedRef = tavernRef.collection('feed').doc(snapshot.id);
    batch.set(feedRef, { checkInId: snapshot.id, userId, createdAt: checkIn.createdAt || admin.firestore.FieldValue.serverTimestamp() }, { merge: true });
    for (const period of periodKeys(completedAt)) {
      const entryRef = tavernRef.collection('leaderboards').doc(period).collection('entries').doc(userId);
      batch.set(entryRef, {
        userId,
        xp: admin.firestore.FieldValue.increment(officialXp),
        checkIns: admin.firestore.FieldValue.increment(1),
        lastActivityDate: new Date(completedAt).toISOString().slice(0, 10),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      }, { merge: true });
    }
  });
  await batch.commit();

  // Notifications are sent only after the authoritative write succeeds.
  const tokens = [];
  const user = await db.doc(`users/${userId}`).get();
  const userTokens = user.exists ? (user.data().fcmTokens || []) : [];
  if (Array.isArray(userTokens)) tokens.push(...userTokens.filter((token) => typeof token === 'string'));
  if (tokens.length > 0) {
    await admin.messaging().sendEachForMulticast({
      tokens: [...new Set(tokens)],
      notification: { title: 'Check-in validado', body: `+${officialXp} XP foi confirmado na sua aventura.` },
      data: { checkInId: snapshot.id, type: 'CHECK_IN_VALIDATED' },
    });
  }
});
