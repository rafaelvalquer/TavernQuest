import { readFile } from 'node:fs/promises';
import { after, afterEach, before, describe, it } from 'node:test';
import { assertFails, assertSucceeds, initializeTestEnvironment } from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc } from 'firebase/firestore';

const projectId = 'demo-tavernquest';
let env;

before(async () => {
  env = await initializeTestEnvironment({
    projectId,
    firestore: { rules: await readFile(new URL('../firestore.rules', import.meta.url), 'utf8') },
  });
});

afterEach(async () => env.clearFirestore());
after(async () => env.cleanup());

describe('Firestore production rules', () => {
  it('permits the sync existence check without exposing another user check-in', async () => {
    const alice = env.authenticatedContext('alice').firestore();
    const bob = env.authenticatedContext('bob').firestore();
    const path = 'checkins/new-checkin';
    await assertSucceeds(getDoc(doc(alice, path)));
    await assertFails(getDoc(doc(env.unauthenticatedContext().firestore(), path)));
    await assertSucceeds(setDoc(doc(alice, path), {
      userId: 'alice', status: 'PENDING_SYNC', xpEarned: 0, occurrenceId: 'mission',
    }));
    await assertSucceeds(getDoc(doc(alice, path)));
    await assertFails(getDoc(doc(bob, path)));
  });
  it('blocks direct invite lookup and client changes to rate limits', async () => {
    await env.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), 'invites/ABCDEF'), { tavernId: 'tavern' });
    });
    const alice = env.authenticatedContext('alice').firestore();
    await assertFails(getDoc(doc(alice, 'invites/ABCDEF')));
    await assertFails(setDoc(doc(alice, 'users/alice/requestLimits/resolveInvite'), { count: 0 }));
  });
  it('allows a user to read only their own profile', async () => {
    await env.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), 'users/alice'), { userId: 'alice', name: 'Alice' });
    });
    await assertSucceeds(getDoc(doc(env.authenticatedContext('alice').firestore(), 'users/alice')));
    await assertFails(getDoc(doc(env.authenticatedContext('bob').firestore(), 'users/alice')));
  });

  it('only permits a pending check-in owned by the authenticated user', async () => {
    const alice = env.authenticatedContext('alice').firestore();
    const bob = env.authenticatedContext('bob').firestore();
    const valid = { userId: 'alice', status: 'PENDING_SYNC', xpEarned: 0, occurrenceId: '2026-09-10_mission' };
    await assertSucceeds(setDoc(doc(alice, 'checkins/alice-checkin'), valid));
    await assertFails(setDoc(doc(bob, 'checkins/bob-for-alice'), valid));
    await assertFails(setDoc(doc(alice, 'checkins/forged-xp'), { ...valid, xpEarned: 9999 }));
  });

  it('allows an active member to read a tavern but blocks client ownership writes', async () => {
    await env.withSecurityRulesDisabled(async (context) => {
      const db = context.firestore();
      await setDoc(doc(db, 'taverns/t1'), { id: 't1', ownerId: 'owner' });
      await setDoc(doc(db, 'taverns/t1/members/alice'), { userId: 'alice', status: 'ACTIVE' });
    });
    const alice = env.authenticatedContext('alice').firestore();
    await assertSucceeds(getDoc(doc(alice, 'taverns/t1')));
    await assertFails(setDoc(doc(alice, 'taverns/t1'), { ownerId: 'alice' }));
    await assertFails(setDoc(doc(alice, 'taverns/t1/members/alice'), { userId: 'alice', status: 'ACTIVE' }));
  });

  it('never allows direct writes to server-owned mission state', async () => {
    const alice = env.authenticatedContext('alice').firestore();
    await assertFails(setDoc(doc(alice, 'userMissions/alice/items/2026-09-10_mission'), {
      userId: 'alice', missionId: 'mission', status: 'COMPLETED', date: '2026-09-10',
    }));
  });
});
