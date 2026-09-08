# Backend Firebase do TavernQuest

O diretório contém a configuração versionável do backend. Ele não inclui credenciais nem `google-services.json`.

O projeto Firebase provisionado é `tavernquest-7684e`. O arquivo de
configuração Android já foi obtido pela CLI e está em `app/google-services.json`
(ignorado pelo Git). Para repetir o deploy:

```powershell
firebase use tavernquest-7684e
firebase deploy --only firestore,storage,functions --project tavernquest-7684e
```

`seedMissionCatalog` inicializa automaticamente as 50 missões oficiais na primeira
criação de perfil em `users/{uid}`. As Functions usam Node.js 22 e têm política de
limpeza de imagens de build após sete dias.

O app já contém os clientes Android para Authentication, Firestore, Storage, FCM, Crashlytics e Analytics. O plugin Google Services só é aplicado quando `app/google-services.json` existe; sem ele, o modo offline continua disponível e a sincronização retorna para a fila.

O cliente publica um documento em `checkins/{checkInId}` com `status: "PENDING_SYNC"`. A Function `validateCheckIn` confere a missão, o XP oficial e a associação do jogador às Tabernas. O documento original é atualizado para `VALIDATED` e cada Taberna recebe apenas uma referência em `taverns/{tavernId}/feed/{checkInId}`.

As Functions também notificam os membros existentes quando uma pessoa entra em uma Taberna e notificam o autor quando um check-in é validado.

Criar, entrar ou sair de uma Taberna grava a mesma associação em `taverns/{tavernId}` e `members/{uid}`. Ao criar, o app também publica `invites/{codigo}` para que outro dispositivo encontre a Taberna pelo código sem precisar de acesso prévio ao grupo. Essas gravações são best-effort: a cópia local permanece disponível quando o usuário está offline ou ainda não configurou o Firebase.
