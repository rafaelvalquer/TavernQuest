import { readFile } from 'node:fs/promises';
import { after, before, describe, it } from 'node:test';
import { assertFails, assertSucceeds, initializeTestEnvironment } from '@firebase/rules-unit-testing';
import { ref, uploadBytes, getMetadata, deleteObject } from 'firebase/storage';

let env;
const path = 'users/alice/checkins/checkin-1/proof.jpg';
before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-tavernquest',
    storage: { rules: await readFile(new URL('../storage.rules', import.meta.url), 'utf8') },
  });
});
after(async () => { await env?.cleanup(); });

describe('Storage production rules', () => {
  it('allows only the owner to upload and read photo metadata', async () => {
    const own = ref(env.authenticatedContext('alice').storage(), path);
    await assertSucceeds(uploadBytes(own, new Uint8Array(32), { contentType: 'image/jpeg' }));
    await assertSucceeds(getMetadata(own));
    await assertFails(getMetadata(ref(env.authenticatedContext('bob').storage(), path)));
    await assertFails(getMetadata(ref(env.unauthenticatedContext().storage(), path)));
  });
  it('rejects anonymous uploads and writes to another user path', async () => {
    for (const context of [env.unauthenticatedContext(), env.authenticatedContext('bob')]) {
      await assertFails(uploadBytes(ref(context.storage(), path), new Uint8Array(32), { contentType: 'image/jpeg' }));
    }
  });
  it('rejects non-images and files at or over the 2 MiB limit', async () => {
    const own = ref(env.authenticatedContext('alice').storage(), path);
    await assertFails(uploadBytes(own, new Uint8Array(32), { contentType: 'application/pdf' }));
    await assertFails(uploadBytes(own, new Uint8Array(2 * 1024 * 1024), { contentType: 'image/jpeg' }));
    await assertFails(uploadBytes(own, new Uint8Array(2 * 1024 * 1024 + 1), { contentType: 'image/jpeg' }));
  });
  it('rejects uploads outside check-in paths and client deletion', async () => {
    const storage = env.authenticatedContext('alice').storage();
    await assertFails(uploadBytes(ref(storage, 'users/alice/avatar.jpg'), new Uint8Array(32), { contentType: 'image/jpeg' }));
    await assertFails(deleteObject(ref(storage, path)));
  });
});
