const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const { onCall, HttpsError } = require('firebase-functions/v2/https');
const { setGlobalOptions } = require('firebase-functions/v2');
const admin = require('firebase-admin');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const missionCatalog = require('./missions.json');
const DEPLOY_REVISION = 2;
const INVITE_ALPHABET = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';

function inviteCodeFromId(id) {
  let value = [...id].reduce((acc, char) => (acc * 31n + BigInt(char.codePointAt(0))) & 0x7fffffffffffffffn, 7n);
  let code = '';
  for (let i = 0; i < 6; i += 1) { code += INVITE_ALPHABET[Number(value % BigInt(INVITE_ALPHABET.length))]; value /= BigInt(INVITE_ALPHABET.length); }
  return code;
}

function hasHeroProfile(data) {
  return typeof data?.name === 'string' && data.name.trim().length >= 2
    && typeof data.heroClass === 'string' && typeof data.appearance === 'string';
}

admin.initializeApp();
setGlobalOptions({ maxInstances: 10, region: 'southamerica-east1' });

exports.bootstrapProfile = onCall(async (request) => {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login com Google.');
  const name = String(request.data?.name || '').trim();
  const heroClass = String(request.data?.heroClass || '');
  const appearance = String(request.data?.appearance || '');
  if (name.length < 2 || name.length > 32) throw new HttpsError('invalid-argument', 'Informe um nome entre 2 e 32 caracteres.');
  if (!['WARRIOR', 'MAGE', 'RANGER', 'ARTISAN', 'GUARDIAN'].includes(heroClass)) throw new HttpsError('invalid-argument', 'Classe inválida.');
  if (!['MASCULINE', 'FEMININE'].includes(appearance)) throw new HttpsError('invalid-argument', 'Aparência inválida.');
  const ref = getFirestore().doc(`users/${request.auth.uid}`);
  const existing = await ref.get();
  const existingData = existing.data() || {};
  const hasHero = typeof existingData.name === 'string' && existingData.name.trim().length >= 2
    && typeof existingData.heroClass === 'string' && typeof existingData.appearance === 'string';
  if (!hasHero) {
    await ref.set({
      userId: request.auth.uid,
      name,
      heroClass,
      appearance,
      createdAt: existingData.createdAt || FieldValue.serverTimestamp(),
      heroCreatedAt: FieldValue.serverTimestamp(),
      authProvider: 'google.com',
      accountCreatedAt: existingData.accountCreatedAt || FieldValue.serverTimestamp(),
      fcmTokens: Array.isArray(existingData.fcmTokens) ? existingData.fcmTokens : [],
    }, { merge: true });
  }
  return { userId: request.auth.uid, created: !hasHero };
});

// This intentionally creates only an account record. Hero data is added later by
// bootstrapProfile, so notification registration cannot race profile creation.
exports.ensureUserAccount = onCall(async (request) => {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login com Google.');
  const ref = getFirestore().doc(`users/${request.auth.uid}`);
  const existing = await ref.get();
  const data = existing.data() || {};
  const token = request.auth.token || {};
  await ref.set({
    userId: request.auth.uid,
    authProvider: 'google.com',
    accountCreatedAt: data.accountCreatedAt || FieldValue.serverTimestamp(),
    lastLoginAt: FieldValue.serverTimestamp(),
    googleDisplayName: typeof token.name === 'string' ? token.name : (data.googleDisplayName || null),
    googlePhotoUrl: typeof token.picture === 'string' ? token.picture : (data.googlePhotoUrl || null),
    timezone: String(request.data?.timezone || data.timezone || 'UTC').slice(0, 64),
    fcmTokens: Array.isArray(data.fcmTokens) ? data.fcmTokens : [],
  }, { merge: true });
  return { userId: request.auth.uid, hasHero: typeof data.name === 'string' && data.name.trim().length >= 2 };
});

exports.registerDeviceToken = onCall(async (request) => {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login com Google.');
  const token = String(request.data?.token || '').trim();
  if (token.length < 20 || token.length > 4096) throw new HttpsError('invalid-argument', 'Token de notificação inválido.');
  await getFirestore().doc(`users/${request.auth.uid}`).set({
    userId: request.auth.uid,
    authProvider: 'google.com',
    fcmTokens: FieldValue.arrayUnion(token),
    lastLoginAt: FieldValue.serverTimestamp(),
  }, { merge: true });
  return { ok: true };
});

exports.createTavern = onCall(async (request) => {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login para criar uma Taberna.');
  const name = String(request.data?.name || '').trim();
  if (!name || name.length > 64) throw new HttpsError('invalid-argument', 'Dados da Taberna inválidos.');
  const db = getFirestore();
  const user = await db.doc(`users/${request.auth.uid}`).get();
  if (!user.exists || !hasHeroProfile(user.data())) throw new HttpsError('failed-precondition', 'Crie seu herói antes de criar uma Taberna.');
  for (let attempt = 0; attempt < 8; attempt += 1) {
    const tavernRef = db.collection('taverns').doc();
    const code = inviteCodeFromId(tavernRef.id);
    const inviteRef = db.collection('invites').doc(code);
    const created = await db.runTransaction(async (transaction) => {
      if ((await transaction.get(inviteRef)).exists) return false;
      const data = { id: tavernRef.id, name, description: String(request.data?.description || '').trim().slice(0, 280), emblem: String(request.data?.emblem || 'WOLF'), private: request.data?.private !== false, ownerId: request.auth.uid, inviteCode: code, createdAt: FieldValue.serverTimestamp() };
      transaction.create(tavernRef, data);
      transaction.create(inviteRef, { ...data, tavernId: tavernRef.id, code });
      transaction.create(tavernRef.collection('members').doc(request.auth.uid), { userId: request.auth.uid, heroId: request.auth.uid, role: 'OWNER', status: 'ACTIVE', joinedAt: FieldValue.serverTimestamp() });
      return true;
    });
    if (created) return { tavernId: tavernRef.id, inviteCode: code, createdAtMillis: Date.now() };
  }
  throw new HttpsError('aborted', 'Não foi possível reservar um código de convite. Tente novamente.');
});

exports.joinTavern = onCall(async (request) => {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login para entrar em uma Taberna.');
  const code = String(request.data?.inviteCode || '').trim().toUpperCase(); const db = getFirestore(); const invite = await db.collection('invites').doc(code).get(); if (!invite.exists) throw new HttpsError('not-found', 'Convite inválido.'); const user = await db.doc(`users/${request.auth.uid}`).get(); if (!user.exists || !hasHeroProfile(user.data())) throw new HttpsError('failed-precondition', 'Crie seu herói antes de entrar em uma Taberna.'); const tavernId = invite.data().tavernId; const member = db.collection('taverns').doc(tavernId).collection('members').doc(request.auth.uid); const existing = await member.get(); if (!existing.exists) await member.create({ userId: request.auth.uid, heroId: request.auth.uid, role: 'MEMBER', status: 'ACTIVE', joinedAt: FieldValue.serverTimestamp() }); else if (existing.data().status !== 'ACTIVE') await member.update({ status: 'ACTIVE', joinedAt: FieldValue.serverTimestamp(), leftAt: FieldValue.delete() }); return { tavernId };
});

function timestampToMillis(value) {
  if (value && typeof value.toMillis === 'function') return value.toMillis();
  return typeof value === 'number' ? value : 0;
}

exports.resolveInvite = onCall(async (request) => {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login.');
  const code = String(request.data?.inviteCode || '').trim().toUpperCase();
  const invite = await getFirestore().collection('invites').doc(code).get();
  if (!invite.exists) throw new HttpsError('not-found', 'Código não encontrado.');
  const data = invite.data();
  return { id: data.tavernId, name: data.name, description: data.description || '', emblem: data.emblem || 'WOLF', private: data.private !== false, inviteCode: code, createdAtMillis: timestampToMillis(data.createdAt) };
});

exports.leaveTavern = onCall(async (request) => { if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login.'); const ref = getFirestore().collection('taverns').doc(String(request.data?.tavernId || '')).collection('members').doc(request.auth.uid); const member = await ref.get(); if (!member.exists) throw new HttpsError('not-found', 'Você não participa desta Taberna.'); if (member.data().role === 'OWNER') throw new HttpsError('failed-precondition', 'OWNER_CANNOT_LEAVE'); await ref.update({ status: 'LEFT', leftAt: FieldValue.serverTimestamp() }); return { ok: true }; });

function periodKeys(timestamp) {
  const date = new Date(timestamp);
  const month = `${date.getUTCFullYear()}-${String(date.getUTCMonth() + 1).padStart(2, '0')}`;
  const firstDay = new Date(Date.UTC(date.getUTCFullYear(), 0, 1));
  const week = Math.ceil((((date - firstDay) / 86400000) + firstDay.getUTCDay() + 1) / 7);
  return [`week-${date.getUTCFullYear()}-W${String(week).padStart(2, '0')}`, `month-${month}`, 'all'];
}

async function ensureMissionCatalog(db) {
  const catalogRef = db.doc('_system/missionCatalog');
  const catalog = await catalogRef.get();
  if (catalog.exists) return;

  const batch = db.batch();
  missionCatalog.forEach((mission) => {
    batch.set(db.doc(`missions/${mission.id}`), {
      ...mission,
      updatedAt: FieldValue.serverTimestamp(),
    }, { merge: true });
  });
  batch.set(catalogRef, {
    version: 1,
    missionCount: missionCatalog.length,
    seededAt: FieldValue.serverTimestamp(),
  });
  await batch.commit();
}

// The catalog is server-owned. Creating a user profile is the first online
// action in the app, so it also makes a fresh project ready for check-ins.
exports.seedMissionCatalog = onDocumentCreated('users/{userId}', async () => {
  void DEPLOY_REVISION;
  await ensureMissionCatalog(getFirestore());
});

exports.validateCheckIn = onDocumentCreated('checkins/{checkInId}', async (event) => {
  const snapshot = event.data;
  if (!snapshot) return;
  const checkIn = snapshot.data();
  if (checkIn.status !== 'PENDING_SYNC') return;

  const db = getFirestore();
  await ensureMissionCatalog(db);
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
  const occurrenceId = String(checkIn.occurrenceId || checkIn.id || '');
  if (!occurrenceId) {
    await checkInRef.update({ status: 'REJECTED', rejectionReason: 'MISSING_OCCURRENCE' });
    return;
  }
  const duplicateSnapshot = await db.collection('checkins')
    .where('userId', '==', userId)
    .where('occurrenceId', '==', occurrenceId)
    .where('status', 'in', ['PENDING_SYNC', 'VALIDATED'])
    .get();
  const duplicate = duplicateSnapshot.docs.find((doc) => doc.id !== snapshot.id);
  if (duplicate) {
    await checkInRef.update({ status: 'REJECTED', rejectionReason: 'MISSION_ALREADY_COMPLETED' });
    return;
  }
  const memberships = await db.collectionGroup('members').where('userId', '==', userId).where('status', '==', 'ACTIVE').get();
  const activityDate = new Date(completedAt).toISOString().slice(0, 10);
  const eligibleTaverns = memberships.docs.map((member) => {
    const tavernRef = member.ref.parent.parent;
    const joinedAt = timestampToMillis(member.data().joinedAt);
    return tavernRef && joinedAt <= completedAt ? tavernRef : null;
  }).filter(Boolean);
  const missionLockRef = db.doc(`missionLocks/${encodeURIComponent(userId)}_${encodeURIComponent(occurrenceId)}`);
  const statsRef = db.doc(`userStats/${userId}`);
  const userDayRef = db.doc(`userActivityDays/${encodeURIComponent(userId)}_${activityDate}`);
  const tavernDays = eligibleTaverns.map((tavernRef) => tavernRef.collection('activityDays').doc(`${userId}_${activityDate}`));
  const validated = await db.runTransaction(async (transaction) => {
    // Read every lock before writing. Firestore retries the transaction if another
    // check-in reaches the same mission or the same activity day concurrently.
    const [missionLock, userDay, ...tavernDaySnapshots] = await Promise.all(
      [missionLockRef, userDayRef, ...tavernDays].map((ref) => transaction.get(ref)),
    );
    if (missionLock.exists) {
      transaction.update(checkInRef, { status: 'REJECTED', rejectionReason: 'MISSION_ALREADY_COMPLETED' });
      return false;
    }
    transaction.create(missionLockRef, { userId, occurrenceId, missionId: checkIn.missionId, checkInId: snapshot.id, createdAt: FieldValue.serverTimestamp() });
    transaction.update(checkInRef, { status: 'VALIDATED', xpEarned: officialXp, validatedAt: FieldValue.serverTimestamp() });
    if (!userDay.exists) transaction.create(userDayRef, { userId, date: activityDate, createdAt: FieldValue.serverTimestamp() });
    transaction.set(statsRef, {
      userId,
      totalXp: FieldValue.increment(officialXp),
      totalCheckIns: FieldValue.increment(1),
      activeSeconds: FieldValue.increment(Math.max(0, durationSeconds)),
      ...(userDay.exists ? {} : { activeDays: FieldValue.increment(1) }),
      updatedAt: FieldValue.serverTimestamp(),
    }, { merge: true });
    eligibleTaverns.forEach((tavernRef, index) => {
      const isNewActiveDay = !tavernDaySnapshots[index].exists;
      if (isNewActiveDay) transaction.create(tavernDays[index], { userId, date: activityDate, createdAt: FieldValue.serverTimestamp() });
      transaction.set(tavernRef.collection('feed').doc(snapshot.id), {
        checkInId: snapshot.id,
        userId,
        occurrenceId,
        missionId: checkIn.missionId,
        missionTitle: String(mission.data().title || 'Missão'),
        category: String(mission.data().category || 'HOME_COMMUNITY'),
        xpEarned: officialXp,
        startedAt,
        completedAt,
        durationSeconds,
        notes: typeof checkIn.notes === 'string' ? checkIn.notes.slice(0, 500) : null,
        photoStoragePath: typeof checkIn.photoStoragePath === 'string' ? checkIn.photoStoragePath : null,
        createdAt: checkIn.createdAt || FieldValue.serverTimestamp(),
      }, { merge: true });
      for (const period of periodKeys(completedAt)) {
        transaction.set(tavernRef.collection('leaderboards').doc(period).collection('entries').doc(userId), {
          userId,
          xp: FieldValue.increment(officialXp),
          checkIns: FieldValue.increment(1),
          ...(isNewActiveDay ? { activeDays: FieldValue.increment(1) } : {}),
          lastActivityDate: activityDate,
          updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
      }
    });
    return true;
  });
  if (!validated) return;

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

exports.notifyTavernMemberJoined = onDocumentCreated('taverns/{tavernId}/members/{memberId}', async (event) => {
  const membership = event.data;
  if (!membership) return;
  const joinedUserId = membership.data().userId;
  const tavernId = event.params.tavernId;
  const db = getFirestore();
  const [tavern, members] = await Promise.all([
    db.doc(`taverns/${tavernId}`).get(),
    db.collection(`taverns/${tavernId}/members`).get(),
  ]);
  const recipients = members.docs
    .map((member) => member.data().userId)
    .filter((userId) => typeof userId === 'string' && userId !== joinedUserId);
  if (recipients.length === 0) return;
  const users = await Promise.all(recipients.map((userId) => db.doc(`users/${userId}`).get()));
  const tokens = users.flatMap((user) => user.exists && Array.isArray(user.data().fcmTokens)
    ? user.data().fcmTokens.filter((token) => typeof token === 'string')
    : []);
  if (tokens.length === 0) return;
  await admin.messaging().sendEachForMulticast({
    tokens: [...new Set(tokens)],
    notification: { title: 'Novo aventureiro', body: `Um novo membro entrou em ${tavern.exists ? tavern.data().name : 'sua Taberna'}.` },
    data: { tavernId, type: 'TAVERN_MEMBER_JOINED' },
  });
});
