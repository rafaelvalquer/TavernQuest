# TavernQuest

Aplicativo Android nativo em Kotlin + Jetpack Compose, com funcionamento offline-first e preparação para grupos sociais Firebase.

## Funcionalidades implementadas
- Onboarding: criação de herói (nome, classe e aparência), fundação de Taberna, entrada com código de convite ou continuação no modo solo.
- Banco Room local com herói, taberna, membros, templates, mural diário, conclusões e ledger de XP.
- 50 contratos iniciais, 10 por categoria.
- Mural diário determinístico com 20 contratos (4 por categoria).
- Limite de 5 contratos aceitos por dia.
- Aceitar, abandonar (camada de domínio/repositório) e concluir missões.
- Conclusão atômica: registro da conclusão, ledger único, XP do herói e status COMPLETED.
- Perfil com nível derivado de XP e Chama derivada do histórico.
- Diário local de conclusões.
- Preferências DataStore para onboarding, som e vibração.
- Foto opcional de comprovação selecionada pela galeria e salva no armazenamento privado do app.
- WorkManager sincroniza a fila quando há conectividade, com validação oficial via Firestore e upload opcional para Storage.
- Check-in com timer persistente, duração, status de sincronização e fila offline.
- Dashboard com estatísticas, calendário mensal e detalhe diário.
- Cinco destinos principais: Início, Missões, Tabernas, Ranking e Herói.
- Múltiplas Tabernas locais, códigos de convite e ranking por período.
- Feed local por Taberna usando referências do mesmo check-in global, com migrações Room v1→v7, privacidade/descrição e sequências de atividade.
- Rules, Storage e Cloud Functions versionados em `firebase/`.
- Login/cadastro por e-mail ou Google, sincronização Firebase em segundo plano e notificações FCM quando o projeto estiver configurado.

## Abrir
Abra a pasta `TavernQuest` no Android Studio, sincronize o Gradle e execute em dispositivo/emulador Android 8+ (API 26+).

## Firebase
O projeto Firebase é `tavernquest-7684e` e o package `com.luminor.tavernquest` já está registrado. Coloque o `google-services.json` baixado do Console em `app/` e siga [firebase/README.md](firebase/README.md). Não versionar credenciais.

## Estado da entrega
O fluxo local e os testes Room de check-in/migração estão implementados. O deploy Firebase e a validação com duas contas online precisam ser executados com uma conta Google autenticada.
