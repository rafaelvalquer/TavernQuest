const { getFirestore } = require('firebase-admin/firestore');
const { HttpsError } = require('firebase-functions/v2/https');

// One bounded document per user/action; clients cannot write these subcollections.
async function consumeRateLimit(uid, action, limit, windowMs, now = Date.now()) {
  const ref = getFirestore().doc(`users/${uid}/requestLimits/${action}`);
  await getFirestore().runTransaction(async transaction => {
    const data = (await transaction.get(ref)).data();
    const active = data && Number.isFinite(data.resetAtMillis) && data.resetAtMillis > now;
    const count = active && Number.isInteger(data.count) ? data.count : 0;
    const resetAtMillis = active ? data.resetAtMillis : now + windowMs;
    if (count >= limit) {
      throw new HttpsError('resource-exhausted', 'Muitas tentativas. Aguarde antes de tentar novamente.', {
        retryAfterSeconds: Math.ceil((resetAtMillis - now) / 1000),
      });
    }
    transaction.set(ref, { count: count + 1, resetAtMillis });
  });
}

module.exports = { consumeRateLimit };
