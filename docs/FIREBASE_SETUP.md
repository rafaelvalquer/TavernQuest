# Firebase do TavernQuest

O projeto será criado como `tavernquest-app` (ou `tavernquest-prod` se o ID estiver ocupado). A criação não pode ser automatizada até que uma conta Google seja autenticada no console; a aba do Firebase foi deixada aberta para o dono da conta.

Depois da criação, registrar o app Android com o package `com.luminor.tavernquest`, baixar `google-services.json` para `app/` e ativar:

- Authentication: e-mail/senha e Google;
- Firestore: `users`, `userStats`, `missions`, `checkins`, `taverns/{tavernId}/members`, `feed` e `leaderboards`;
- Storage para comprovantes;
- Functions para validar o XP, impedir duplicidade e fazer fan-out do mesmo `checkInId` em várias Tabernas;
- FCM, Crashlytics e Analytics.

Tokens FCM devem ser armazenados em `users/{userId}.fcmTokens`. A Function só envia a notificação depois do commit que valida o check-in e atualiza os rankings.

O cliente já marca check-ins locais como `PENDING_SYNC`, mantém a fila em Room e exclui itens rejeitados das estatísticas. O ranking remoto só deve contar documentos validados pelo backend.

O payload remoto deve usar `FirebaseCheckInPayload`: ele envia `xpEarned: 0` de propósito. O XP local é provisório e nunca é aceito como autoridade pelo Firestore ou pela Function.

Não adicionar credenciais, `google-services.json` ou chaves de Functions ao controle de versão.

As regras e a Function versionadas em `firebase/` devem ser revisadas no console antes do primeiro deploy de produção.
