const { test, after } = require('node:test');
const assert = require('node:assert/strict');

if (!process.env.FIRESTORE_EMULATOR_HOST || process.env.GCLOUD_PROJECT !== 'demo-tavernquest') {
  throw new Error('Account tests require Firestore Emulator and demo-tavernquest.');
}
const { getApps, deleteApp } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const functions = require('./index');
const request = (uid, data = {}) => ({ auth: { uid, token: {} }, data });
const hero = { name: 'Aventureiro', heroClass: 'WARRIOR', appearance: 'MASCULINE' };
const prefix = `account-test-${Date.now()}`;

test('account operations reject missing authentication', async () => {
  for (const handler of [functions.ensureUserAccount, functions.bootstrapProfile]) {
    await assert.rejects(handler.run({ data: hero }), { code: 'unauthenticated' });
  }
});

test('concurrent bootstrap creates exactly one hero and retry preserves it', async () => {
  const uid = `${prefix}-concurrent`;
  await functions.ensureUserAccount.run(request(uid));
  const results = await Promise.all([
    functions.bootstrapProfile.run(request(uid, hero)),
    functions.bootstrapProfile.run(request(uid, { ...hero, name: 'Outro herói' })),
  ]);
  assert.equal(results.filter(result => result.created).length, 1);
  const ref = getFirestore().doc(`users/${uid}`);
  const initial = (await ref.get()).data();
  await functions.bootstrapProfile.run(request(uid, { ...hero, name: 'Tentativa' }));
  await Promise.all(Array.from({ length: 4 }, () => functions.ensureUserAccount.run(request(uid))));
  const current = (await ref.get()).data();
  assert.equal(current.name, initial.name);
  assert.equal(current.heroCreatedAt.toMillis(), initial.heroCreatedAt.toMillis());
  assert.equal(current.accountCreatedAt.toMillis(), initial.accountCreatedAt.toMillis());
  assert.equal((await functions.ensureUserAccount.run(request(uid))).hasHero, true);
});

test('incomplete and invalid profiles cannot unlock the hero flow', async () => {
  const uid = `${prefix}-incomplete`;
  const ref = getFirestore().doc(`users/${uid}`);
  await ref.set({ name: 'Nome existente', heroClass: 'INVALID', appearance: 'MASCULINE' });
  assert.equal((await functions.ensureUserAccount.run(request(uid))).hasHero, false);
  await assert.rejects(functions.bootstrapProfile.run(request(uid, { ...hero, heroClass: 'INVALID' })), { code: 'invalid-argument' });
  assert.equal((await functions.bootstrapProfile.run(request(uid, hero))).created, true);
});

test('two users share a tavern and duplicate join requests remain idempotent', async () => {
  const owner = `${prefix}-owner`;
  const member = `${prefix}-member`;
  for (const uid of [owner, member]) await functions.bootstrapProfile.run(request(uid, hero));
  const created = await functions.createTavern.run(request(owner, { name: 'Taberna teste', emblem: 'WOLF' }));
  const joined = await Promise.all(Array.from({ length: 3 }, () =>
    functions.joinTavern.run(request(member, { inviteCode: ` ${created.inviteCode.toLowerCase()} ` }))));
  assert.ok(joined.every(result => result.tavernId === created.tavernId));
  const members = getFirestore().collection(`taverns/${created.tavernId}/members`);
  assert.equal((await members.get()).size, 2);
  assert.equal((await members.doc(owner).get()).data().role, 'OWNER');
  await functions.leaveTavern.run(request(member, { tavernId: created.tavernId }));
  await functions.joinTavern.run(request(member, { inviteCode: created.inviteCode }));
  assert.equal((await members.doc(member).get()).data().status, 'ACTIVE');
  await members.doc(member).update({ status: 'BANNED' });
  await assert.rejects(functions.joinTavern.run(request(member, { inviteCode: created.inviteCode })), { code: 'permission-denied' });
});

test('invalid invite syntax is rejected before database access', async () => {
  for (const inviteCode of ['', 'abc/def', null, 123456]) {
    for (const handler of [functions.joinTavern, functions.resolveInvite]) {
      await assert.rejects(handler.run(request(`${prefix}-invalid`, { inviteCode })), { code: 'invalid-argument' });
    }
  }
});

after(async () => { await Promise.all(getApps().map(deleteApp)); });
