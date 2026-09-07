# TavernQuest MVP

Aplicativo Android nativo 100% Kotlin + Jetpack Compose e totalmente offline.

## Funcionalidades implementadas
- Onboarding: criação de herói (nome, classe e aparência) e fundação da Taberna.
- Banco Room local com herói, taberna, membros, templates, mural diário, conclusões e ledger de XP.
- 50 contratos iniciais, 10 por categoria.
- Mural diário determinístico com 20 contratos (4 por categoria).
- Limite de 5 contratos aceitos por dia.
- Aceitar, abandonar (camada de domínio/repositório) e concluir missões.
- Conclusão atômica: registro da conclusão, ledger único, XP do herói e status COMPLETED.
- Perfil com nível derivado de XP e Chama derivada do histórico.
- Diário local de conclusões.
- Preferências DataStore para onboarding, som e vibração.
- Configuração de câmera/FileProvider preparada; foto é opcional no MVP atual.

## Abrir
Abra a pasta `TavernQuest` no Android Studio, sincronize o Gradle e execute em dispositivo/emulador Android 8+ (API 26+).

## Observação
O projeto não contém APK/AAB. Assets visuais do plano foram representados por componentes Compose e placeholders leves; podem ser trocados por arte final sem mudar a arquitetura.
