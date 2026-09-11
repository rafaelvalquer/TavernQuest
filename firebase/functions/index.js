const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const { onCall, HttpsError } = require('firebase-functions/v2/https');
const { setGlobalOptions } = require('firebase-functions/v2');
const admin = require('firebase-admin');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const missionCatalog = require('./missions.json');
const { consumeRateLimit } = require('./rate-limit');
const { validCheckInTiming } = require('./checkin-validation');
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
    && data.name.trim().length <= 32
    && ['WARRIOR', 'MAGE', 'RANGER', 'ARTISAN', 'GUARDIAN'].includes(data.heroClass)
    && ['MASCULINE', 'FEMININE'].includes(data.appearance);
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
  return getFirestore().runTransaction(async (transaction) => {
  const existing = await transaction.get(ref);
  const existingData = existing.data() || {};
  const hasHero = hasHeroProfile(existingData);
  if (!hasHero) {
    transaction.set(ref, {
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
});

// This intentionally creates only an account record. Hero data is added later by
// bootstrapProfile, so notification registration cannot race profile creation.
exports.ensureUserAccount = onCall(async (request) => {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login com Google.');
  const ref = getFirestore().doc(`users/${request.auth.uid}`);
  return getFirestore().runTransaction(async (transaction) => {
  const existing = await transaction.get(ref);
  const data = existing.data() || {};
  const token = request.auth.token || {};
  transaction.set(ref, {
    userId: request.auth.uid,
    authProvider: 'google.com',
    accountCreatedAt: data.accountCreatedAt || FieldValue.serverTimestamp(),
    lastLoginAt: FieldValue.serverTimestamp(),
    googleDisplayName: typeof token.name === 'string' ? token.name : (data.googleDisplayName || null),
    googlePhotoUrl: typeof token.picture === 'string' ? token.picture : (data.googlePhotoUrl || null),
    timezone: String(request.data?.timezone || data.timezone || 'UTC').slice(0, 64),
    fcmTokens: Array.isArray(data.fcmTokens) ? data.fcmTokens : [],
  }, { merge: true });
  return { userId: request.auth.uid, hasHero: hasHeroProfile(data) };
  });
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
  await consumeRateLimit(request.auth.uid, 'createTavern', 5, 10 * 60_000);
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

function normalizedInviteCode(value) {
  if (typeof value !== 'string') throw new HttpsError('invalid-argument', 'Informe um código de convite válido.');
  const code = value.trim().toUpperCase();
  if (!/^[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}$/.test(code)) {
    throw new HttpsError('invalid-argument', 'Informe um código de convite válido.');
  }
  return code;
}

exports.joinTavern = onCall(async (request) => {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login para entrar em uma Taberna.');
  await consumeRateLimit(request.auth.uid, 'joinTavern', 30, 60_000);
  const code = normalizedInviteCode(request.data?.inviteCode);
  const db = getFirestore();
  return db.runTransaction(async (transaction) => {
    const invite = await transaction.get(db.collection('invites').doc(code));
    if (!invite.exists) throw new HttpsError('not-found', 'Convite inválido.');
    const tavernId = invite.data().tavernId;
    if (typeof tavernId !== 'string' || !tavernId || tavernId.includes('/')) {
      throw new HttpsError('not-found', 'Convite inválido.');
    }
    const tavern = db.collection('taverns').doc(tavernId);
    const member = tavern.collection('members').doc(request.auth.uid);
    const [user, tavernSnapshot, existing] = await transaction.getAll(
      db.doc(`users/${request.auth.uid}`), tavern, member,
    );
    if (!tavernSnapshot.exists || tavernSnapshot.data().inviteCode !== code) {
      throw new HttpsError('not-found', 'Convite inválido.');
    }
    if (!user.exists || !hasHeroProfile(user.data())) {
      throw new HttpsError('failed-precondition', 'Crie seu herói antes de entrar em uma Taberna.');
    }
    if (!existing.exists) {
      transaction.create(member, { userId: request.auth.uid, heroId: request.auth.uid, role: 'MEMBER', status: 'ACTIVE', joinedAt: FieldValue.serverTimestamp() });
    } else if (existing.data().status === 'LEFT') {
      transaction.update(member, { status: 'ACTIVE', joinedAt: FieldValue.serverTimestamp(), leftAt: FieldValue.delete() });
    } else if (existing.data().status !== 'ACTIVE') {
      throw new HttpsError('permission-denied', 'Sua participação nesta Taberna não está disponível.');
    }
    return { tavernId };
  });
});

function timestampToMillis(value) {
  if (value && typeof value.toMillis === 'function') return value.toMillis();
  return typeof value === 'number' ? value : 0;
}

async function rejectCheckIn(checkInRef, checkIn, reason) {
  await checkInRef.update({ status: 'REJECTED', rejectionReason: reason });
  const path = typeof checkIn.photoStoragePath === 'string' ? checkIn.photoStoragePath : '';
  const expectedPrefix = `users/${checkIn.userId}/checkins/${checkInRef.id}/`;
  if (path.startsWith(expectedPrefix)) {
    await admin.storage().bucket().file(path).delete({ ignoreNotFound: true }).catch(() => undefined);
  }
}

exports.resolveInvite = onCall(async (request) => {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login.');
  await consumeRateLimit(request.auth.uid, 'resolveInvite', 30, 60_000);
  const code = normalizedInviteCode(request.data?.inviteCode);
  const invite = await getFirestore().collection('invites').doc(code).get();
  if (!invite.exists) throw new HttpsError('not-found', 'Código não encontrado.');
  const data = invite.data();
  return { id: data.tavernId, name: data.name, description: data.description || '', emblem: data.emblem || 'WOLF', private: data.private !== false, inviteCode: code, createdAtMillis: timestampToMillis(data.createdAt) };
});

exports.leaveTavern = onCall(async (request) => { if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login.'); const ref = getFirestore().collection('taverns').doc(String(request.data?.tavernId || '')).collection('members').doc(request.auth.uid); const member = await ref.get(); if (!member.exists) throw new HttpsError('not-found', 'Você não participa desta Taberna.'); if (member.data().role === 'OWNER') throw new HttpsError('failed-precondition', 'OWNER_CANNOT_LEAVE'); await ref.update({ status: 'LEFT', leftAt: FieldValue.serverTimestamp() }); return { ok: true }; });

// Storage paths are never exposed as public URLs. A member receives a short
// lived URL only after the server proves that the check-in is in that Tavern's
// feed and that the member is still ACTIVE.
exports.getCheckInPhotoUrl = onCall(async (request) => {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login.');
  const tavernId = String(request.data?.tavernId || '');
  const checkInId = String(request.data?.checkInId || '');
  if (!tavernId || !checkInId) throw new HttpsError('invalid-argument', 'Foto inválida.');
  const db = getFirestore();
  const [member, feed] = await Promise.all([
    db.doc(`taverns/${tavernId}/members/${request.auth.uid}`).get(),
    db.doc(`taverns/${tavernId}/feed/${checkInId}`).get(),
  ]);
  if (!member.exists || member.data().status !== 'ACTIVE') throw new HttpsError('permission-denied', 'Você não participa desta Taberna.');
  if (!feed.exists) throw new HttpsError('not-found', 'Foto não encontrada.');
  const path = typeof feed.data().photoStoragePath === 'string' ? feed.data().photoStoragePath : '';
  const ownerId = typeof feed.data().userId === 'string' ? feed.data().userId : '';
  const allowedPrefix = `users/${ownerId}/checkins/${checkInId}/`;
  if (!path || !ownerId || !path.startsWith(allowedPrefix)) throw new HttpsError('not-found', 'Foto não encontrada.');
  const [url] = await admin.storage().bucket().file(path).getSignedUrl({ action: 'read', expires: Date.now() + 10 * 60 * 1000 });
  return { url, expiresAtMillis: Date.now() + 10 * 60 * 1000 };
});

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

function missionOccurrenceId(date, missionId) {
  return `${date}_${missionId}`;
}

function timestampOrNull(value) {
  const millis = timestampToMillis(value);
  return millis > 0 ? millis : null;
}

function missionResponse(data) {
  return {
    occurrenceId: data.occurrenceId,
    missionId: data.missionId,
    date: data.date,
    status: data.status,
    acceptedAt: timestampOrNull(data.acceptedAt),
    startedAt: timestampOrNull(data.startedAt),
    completedAt: timestampOrNull(data.completedAt),
  };
}

// The daily board and its state are server-owned so a second device resumes
// the same accepted mission and timer instead of generating a local variant.
exports.getDailyMissionBoard = onCall(async (request) => {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login.');
  const date = String(request.data?.date || '');
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) throw new HttpsError('invalid-argument', 'Data de missão inválida.');
  const requestedDay = Date.parse(`${date}T00:00:00Z`);
  const currentDay = Date.parse(`${new Date().toISOString().slice(0, 10)}T00:00:00Z`);
  if (!Number.isFinite(requestedDay) || Math.abs(requestedDay - currentDay) > 24 * 60 * 60 * 1000) {
    throw new HttpsError('invalid-argument', 'O quadro diário está disponível apenas para a data atual.');
  }
  const db = getFirestore();
  const user = await db.doc(`users/${request.auth.uid}`).get();
  if (!user.exists || !hasHeroProfile(user.data())) throw new HttpsError('failed-precondition', 'Crie seu herói antes de acessar missões.');
  await ensureMissionCatalog(db);
  const missionsRef = db.collection(`userMissions/${request.auth.uid}/items`);
  const existing = await missionsRef.where('date', '==', date).get();
  if (existing.empty) {
    const byCategory = new Map();
    missionCatalog.filter((mission) => mission.enabled).forEach((mission) => {
      const list = byCategory.get(mission.category) || [];
      list.push(mission);
      byCategory.set(mission.category, list);
    });
    const selected = [...byCategory.values()].flatMap((items) => items
      .slice()
      .sort((a, b) => `${date}:${a.id}`.localeCompare(`${date}:${b.id}`))
      .slice(0, 4));
    const dayRef = db.doc(`userMissionDays/${encodeURIComponent(request.auth.uid)}_${date}`);
    await db.runTransaction(async (transaction) => {
      const day = await transaction.get(dayRef);
      if (day.exists) return;
      transaction.create(dayRef, { userId: request.auth.uid, date, acceptedCount: 0, createdAt: FieldValue.serverTimestamp() });
      selected.forEach((mission) => {
        const occurrenceId = missionOccurrenceId(date, mission.id);
        transaction.create(missionsRef.doc(occurrenceId), {
          userId: request.auth.uid, occurrenceId, missionId: mission.id, date,
          status: 'AVAILABLE', createdAt: FieldValue.serverTimestamp(),
        });
      });
    });
  }
  const [items, catalog] = await Promise.all([
    missionsRef.where('date', '==', date).get(),
    db.collection('missions').get(),
  ]);
  const catalogById = new Map(catalog.docs.map((doc) => [doc.id, doc.data()]));
  return { items: items.docs.map((doc) => ({ ...missionResponse(doc.data()), mission: catalogById.get(doc.data().missionId) || null })) };
});

exports.updateMissionStatus = onCall(async (request) => {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Faça login.');
  const occurrenceId = String(request.data?.occurrenceId || '');
  const action = String(request.data?.action || '');
  if (!occurrenceId || !['ACCEPT', 'ABANDON', 'START'].includes(action)) throw new HttpsError('invalid-argument', 'Transição de missão inválida.');
  const db = getFirestore();
  const ref = db.doc(`userMissions/${request.auth.uid}/items/${occurrenceId}`);
  await db.runTransaction(async (transaction) => {
    const item = await transaction.get(ref);
    if (!item.exists) throw new HttpsError('not-found', 'Missão não encontrada.');
    const data = item.data();
    const dayRef = db.doc(`userMissionDays/${encodeURIComponent(request.auth.uid)}_${data.date}`);
    const day = await transaction.get(dayRef);
    if (!day.exists) throw new HttpsError('failed-precondition', 'Quadro diário não encontrado.');
    if (action === 'ACCEPT') {
      if (data.status === 'AVAILABLE') {
        if (Number(day.data().acceptedCount || 0) >= 5) throw new HttpsError('failed-precondition', 'Limite de cinco missões aceitas atingido.');
        transaction.update(ref, { status: 'ACCEPTED', acceptedAt: FieldValue.serverTimestamp() });
        transaction.update(dayRef, { acceptedCount: FieldValue.increment(1), updatedAt: FieldValue.serverTimestamp() });
      }
    } else if (action === 'ABANDON') {
      if (data.status === 'ACCEPTED') {
        transaction.update(ref, { status: 'ABANDONED', abandonedAt: FieldValue.serverTimestamp() });
        transaction.update(dayRef, { acceptedCount: FieldValue.increment(-1), updatedAt: FieldValue.serverTimestamp() });
      }
    } else if (action === 'START' && data.status === 'ACCEPTED' && !data.startedAt) {
      transaction.update(ref, { startedAt: FieldValue.serverTimestamp() });
    }
  });
  // Read after the transaction so the callable returns the server Timestamp
  // rather than an optimistic client clock.
  return missionResponse((await ref.get()).data());
});

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
    await rejectCheckIn(checkInRef, checkIn, 'MISSION_NOT_FOUND');
    return;
  }
  const officialXp = Number(mission.data().xpReward || 0);
  const completedAt = Number(checkIn.completedAt || 0);
  const startedAt = Number(checkIn.startedAt || completedAt);
  const durationSeconds = Number(checkIn.durationSeconds || 0);
  if (!validCheckInTiming(checkIn)) {
    await rejectCheckIn(checkInRef, checkIn, 'INVALID_DURATION');
    return;
  }

  const userId = checkIn.userId;
  const occurrenceId = String(checkIn.occurrenceId || checkIn.id || '');
  if (!occurrenceId) {
    await rejectCheckIn(checkInRef, checkIn, 'MISSING_OCCURRENCE');
    return;
  }
  const duplicateSnapshot = await db.collection('checkins')
    .where('userId', '==', userId)
    .where('occurrenceId', '==', occurrenceId)
    .where('status', 'in', ['PENDING_SYNC', 'VALIDATED'])
    .get();
  const duplicate = duplicateSnapshot.docs.find((doc) => doc.id !== snapshot.id);
  if (duplicate) {
    await rejectCheckIn(checkInRef, checkIn, 'MISSION_ALREADY_COMPLETED');
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
  const missionStateRef = db.doc(`userMissions/${userId}/items/${occurrenceId}`);
  const statsRef = db.doc(`userStats/${userId}`);
  const userDayRef = db.doc(`userActivityDays/${encodeURIComponent(userId)}_${activityDate}`);
  const tavernDays = eligibleTaverns.map((tavernRef) => tavernRef.collection('activityDays').doc(`${userId}_${activityDate}`));
  const validated = await db.runTransaction(async (transaction) => {
    // Read every lock before writing. Firestore retries the transaction if another
    // check-in reaches the same mission or the same activity day concurrently.
    const [missionLock, userDay, missionState, ...tavernDaySnapshots] = await Promise.all(
      [missionLockRef, userDayRef, missionStateRef, ...tavernDays].map((ref) => transaction.get(ref)),
    );
    if (missionLock.exists) {
      transaction.update(checkInRef, { status: 'REJECTED', rejectionReason: 'MISSION_ALREADY_COMPLETED' });
      return false;
    }
    if (!missionState.exists || missionState.data().userId !== userId
      || missionState.data().missionId !== checkIn.missionId
      || missionState.data().status !== 'ACCEPTED') {
      transaction.update(checkInRef, { status: 'REJECTED', rejectionReason: 'MISSION_NOT_ACCEPTED' });
      return false;
    }
    transaction.create(missionLockRef, { userId, occurrenceId, missionId: checkIn.missionId, checkInId: snapshot.id, createdAt: FieldValue.serverTimestamp() });
    transaction.update(checkInRef, { status: 'VALIDATED', xpEarned: officialXp, validatedAt: FieldValue.serverTimestamp() });
    transaction.update(missionStateRef, { status: 'COMPLETED', completedAt: FieldValue.serverTimestamp(), checkInId: snapshot.id });
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
  if (!validated) {
    const rejected = await checkInRef.get();
    if (rejected.data()?.status === 'REJECTED') await rejectCheckIn(checkInRef, checkIn, rejected.data().rejectionReason || 'REJECTED');
    return;
  }

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
