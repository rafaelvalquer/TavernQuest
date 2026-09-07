# Backend Firebase do TavernQuest

O diretório contém a configuração versionável do backend. Ele não inclui credenciais nem `google-services.json`.

Depois de criar o projeto `tavernquest-app` no console:

```powershell
firebase use tavernquest-app
firebase deploy --only firestore:rules,storage,functions
```

O cliente publica um documento em `checkins/{checkInId}` com `status: "PENDING_SYNC"`. A Function `validateCheckIn` confere a missão, o XP oficial e a associação do jogador às Tabernas. O documento original é atualizado para `VALIDATED` e cada Taberna recebe apenas uma referência em `taverns/{tavernId}/feed/{checkInId}`.
