# Backend Firebase do TavernQuest

O diretório contém a configuração versionável do backend. Ele não inclui credenciais nem `google-services.json`.

O projeto Firebase provisionado é `tavernquest-7684e`. O arquivo de
configuração Android já foi obtido pela CLI e está em `app/google-services.json`
(ignorado pelo Git). Para repetir o deploy:

```powershell
firebase use prod
firebase deploy --only firestore,storage,functions --project prod
```

Os aliases versionados são `prod` (`tavernquest-7684e`) e `dev`
(`tavernquest-dev-7684e`). Nunca execute um deploy de teste usando o alias de
produção.

`seedMissionCatalog` inicializa automaticamente as 50 missões oficiais na primeira
criação de perfil em `users/{uid}`. As Functions usam Node.js 22 e têm política de
limpeza de imagens de build após sete dias.

O app já contém os clientes Android para Authentication, Firestore, Storage, FCM, Crashlytics e Analytics. O APK release exige `app/google-services.json` e assinatura configurada; o Room é somente cache e fila offline.

O cliente publica um documento em `checkins/{checkInId}` com `status: "PENDING_SYNC"`. A Function `validateCheckIn` confere a missão, o XP oficial e a associação do jogador às Tabernas. O documento original é atualizado para `VALIDATED` e cada Taberna recebe apenas uma referência em `taverns/{tavernId}/feed/{checkInId}`.

As Functions também notificam os membros existentes quando uma pessoa entra em uma Taberna e notificam o autor quando um check-in é validado. O quadro diário fica em `userMissions/{uid}/items/{occurrenceId}` e é alterado somente por Functions.

Criar, entrar ou sair de uma Taberna é executado pelas Functions. Ao criar, o servidor reserva `taverns/{tavernId}`, o membro proprietário e `invites/{codigo}` de forma transacional. O cliente observa o Firestore depois da confirmação.

## Testes de regras

Com JDK 21 e Node.js instalados, execute no Windows:

```powershell
$env:JAVA_HOME='C:\JAVA\JDK-21'
$env:Path='C:\JAVA\JDK-21\bin;'+$env:Path
Set-Location firebase/tests
npm install
npm run test:rules
```

Os testes usam somente o Firebase Emulator e verificam que perfis, check-ins,
tabernas e missões server-owned não podem ser adulterados diretamente pelo cliente.

Para executar a variante Android `dev` contra os emuladores locais no Android
Studio, suba os emuladores no diretório `firebase` e gere o APK com:

```powershell
./gradlew :app:assembleDevDebug -PuseFirebaseEmulators=true
```

O endereço `10.0.2.2` é o host visto pelo emulador Android. A variante `prod`
nunca usa esses endpoints.
