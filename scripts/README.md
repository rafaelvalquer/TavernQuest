# Gerar APK para instalação direta

No terminal PowerShell, na pasta do projeto:

```powershell
.\scripts\build-production.ps1
```

O comando exige o `app/google-services.json` de produção e a assinatura configurada em `app/release.properties`. Esses arquivos e o keystore permanecem fora do Git. Requer Java 21 e Android SDK; para outro caminho do SDK, informe `-AndroidSdk 'C:\caminho\Android\Sdk'`.

Executa `lintProdRelease`, `testProdReleaseUnitTest` e `assembleProdRelease`. Só exporta o APK após verificar assinatura, certificado de produção, correspondência do cliente OAuth Android no JSON, pacote e variante. A saída fica em `build/distribution`, com versão no nome e arquivo `.sha256` para conferir a integridade da transferência.

O menu Generate APK do Android Studio depende da variante selecionada. Para distribuição, use este comando. APKs antigos em outras pastas de build não são a saída deste comando.

Essa validação não comprova o estado atual do Firebase Console nem o login real. Antes de distribuir publicamente, validar login Google, criação de perfil, tabernas, logout e retorno em celulares físicos, além das demais condições de lançamento do plano. Não desinstalar uma versão existente sem considerar a perda de dados locais.

## Validação de Pull Requests

O workflow usa `prepare-ci.py` para criar configuração Firebase fictícia e certificado descartável. O script só aceita `CI=true` e recusa substituir arquivos existentes. O APK produzido pelo workflow verifica o código `prodRelease`, mas não serve para distribuição nem login real. A chave de produção não é disponibilizada a Pull Requests. A geração assinada para usuários continua pelo comando de produção acima; o pipeline de release por tag ainda está pendente.
