# Correções desta versão

- Mantém Gradle executando com JDK 21 e bytecode JVM 17.
- Remove `jvmToolchain(17)` para não exigir JDK 17 instalado separadamente.
- Corrige tipos de função Compose em:
  - ParchmentBackground.kt
  - TavernBackground.kt
  - TavernTheme.kt
  - WoodPanel.kt
  - ParchmentCard.kt
- Corrige `Arrangement.spacedBy(8.dp)` em QuestActions.kt.
- Varredura de sintaxe Kotlin realizada nos arquivos de `app/src/main/java` sem diagnósticos `Type expected`, `expecting` ou `unexpected tokens`.
