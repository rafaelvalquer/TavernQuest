const fs = require('fs');
const path = require('path');
const admin = require('firebase-admin');

admin.initializeApp();

async function main() {
  const missions = JSON.parse(fs.readFileSync(path.join(__dirname, '..', 'missions.json'), 'utf8'));
  const db = admin.firestore();
  const batch = db.batch();
  missions.forEach((mission) => batch.set(db.doc(`missions/${mission.id}`), { ...mission, updatedAt: admin.firestore.FieldValue.serverTimestamp() }, { merge: true }));
  await batch.commit();
  console.log(`Seeded ${missions.length} missions.`);
}

main().catch((error) => { console.error(error); process.exitCode = 1; });
