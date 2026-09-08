# Firebase do TavernQuest

O projeto criado é `tavernquest-7684e`, na conta autenticada no Firebase Console. O Firestore padrão está em `southamerica-east1`, e Authentication por e-mail/senha e Google já estão ativos.

O app Android `com.luminor.tavernquest` já está registrado. Baixe `google-services.json` atualizado para `app/`; o SHA-1 de debug também já está registrado. Ainda é necessário ativar Storage e implantar Functions, o que exige um plano Firebase com faturamento.

- Authentication: e-mail/senha e Google (o client ID web é lido do `google-services.json`);
- Firestore: `users`, `userStats`, `missions`, `checkins`, `taverns/{tavernId}/members`, `feed` e `leaderboards`;
- Storage para comprovantes;
- Functions para validar o XP, impedir duplicidade e fazer fan-out do mesmo `checkInId` em várias Tabernas;
- FCM, Crashlytics e Analytics.

Quando `app/google-services.json` existir, o Gradle aplica automaticamente o plugin Google Services. Sem esse arquivo, o app continua iniciando em modo offline e as telas de autenticação informam que a configuração Firebase está ausente.

Tokens FCM devem ser armazenados em `users/{userId}.fcmTokens`. A Function só envia a notificação depois do commit que valida o check-in e atualiza os rankings.

O cliente já marca check-ins locais como `PENDING_SYNC`, mantém a fila em Room e exclui itens rejeitados das estatísticas. O ranking remoto só deve contar documentos validados pelo backend.

O payload remoto deve usar `FirebaseCheckInPayload`: ele envia `xpEarned: 0` de propósito. O XP local é provisório e nunca é aceito como autoridade pelo Firestore ou pela Function.

Não adicionar credenciais, `google-services.json` ou chaves de Functions ao controle de versão.

As regras e a Function versionadas em `firebase/` devem ser revisadas no console antes do primeiro deploy de produção.
