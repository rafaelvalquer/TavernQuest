# Preparação para distribuição por APK

Estado em 10/09/2026: **lançamento público ainda não aprovado**. O canal inicial é instalação direta, sem Google Play. Este registro acompanha o plano de produção; uma compilação bem-sucedida não substitui os critérios abaixo.

## Evidências locais confirmadas

- `scripts/build-production.ps1` concluiu lintProdRelease, testProdReleaseUnitTest e assembleProdRelease, verificou assinatura e correspondência do certificado com o cliente OAuth Android no JSON de produção.
- APK exportado: `build/distribution/TavernQuest-1.0.1-2-prodRelease.apk`.
- SHA-256 do arquivo exportado: `20230077f9321d21b823eb5d92fdf7f678176cae49db78473636d792bc221f6e`.
- Pacote: `com.luminor.tavernquest`; projeto configurado: `tavernquest-7684e`; certificado SHA-1: `A0:0B:BE:35:D5:C9:AA:98:7D:EC:B5:0F:D8:05:FC:62:6C:86:1A:CC`.
- compileSdk/targetSdk 36. Provider App Check debug separado por source set e dependência debugImplementation.
- Telemetria de autenticação sanitizada; não anexa mensagem ou causa original contendo dados de conta. Erros de login possuem classificação e referência técnica.
- Lint completo passou após declarar câmera opcional. Permanecem avisos a revisar; não foi aplicado baseline para ocultar erros.
- Firebase Emulator: 8 testes passaram (4 Firestore e 4 Storage), sem falhas. Cobrem isolamento de perfil, check-in pendente, acesso de membro à taberna, bloqueio de escrita de estado de missão, propriedade/tipo/tamanho/caminho de fotos e bloqueio de exclusão pelo cliente. Não cobrem todas as Functions nem App Check.

Essas evidências se referem ao estado local e ao APK identificado acima. Não comprovam que o celular do usuário recebeu esse arquivo ou que as configurações remotas continuam corretas.

## Condições ainda pendentes

| Área | Trabalho e evidência exigida |
| --- | --- |
| Login real | 10/10 tentativas no release, em pelo menos 3 celulares e 2 contas; login, logout, retorno e limpeza de dados. Usuário ainda reporta falha em APK de variante desconhecida e está sem USB. |
| Firebase Console | Confirmar Google ativo, configuração OAuth/certificados atuais e decisão pendente sobre desativação de e-mail/senha. |
| Sessão | Testes de integração do bootstrap, falha/retry e perfil incompleto; comprovar users/{uid} e navegação remota. |
| CI | Finalizar gates prodRelease, análise Kotlin, Functions, regras e migrações. Publicar as mudanças e verificar execução no GitHub. |
| Proteção Git | main somente por PR com checks obrigatórios; verificar estado remoto. |
| Release por tag | Build assinado com segredos protegidos, versão/tag consistentes e artifact + SHA-256; não fornecer chave de produção a PR não confiável. |
| Room | Teste 1→7 ampliado, execução pendente. Recuperar schemas 3/4/5 com origem verificável e testar cada versão histórica até 7 preservando dados. |
| Firebase Emulator | Firestore + Storage: 8 testes passaram após recuperar o download do runtime. Implementar/rodar Functions e ampliar limites/autorizações. |
| App Check | Configurar Play Integrity para APK direto, comprovar atestação em release físico e só depois ativar enforcement em Functions/Firestore/Storage. |
| Segurança servidor | Revisar autenticação, parâmetros, limites, anti-spam, convites, XP, recompensas, feed e ranking. |
| Exclusão de conta | UI com confirmação/reauth, operação remota idempotente, remoção de dados e identidade, testes e canal externo de solicitação. |
| Privacidade | Política pública acessível no app, contato e retenção definidos, descrição de Auth/fotos/Analytics/Crashlytics/FCM. |
| Apresentação | Ícone adaptativo e roundIcon próprios, revisão de splash/nome e acabamento. |
| R8 | Ativar minificação/shrink e testar o APK resultante em Auth, Firebase, Hilt, Room, WorkManager, FCM e Crashlytics. |
| Compatibilidade | APIs 26/29/31/33/34/35/36; notificações, câmera/foto/FileProvider, trabalho em background e permissões negadas. |
| Aceitação funcional | Instalação/upgrade, herói, taberna em duas contas, missões/conclusão/XP/streak, fotos/sync, offline/retorno, push, reinício/troca de conta, erros, App Check/Crashlytics/exclusão. |
| Lançamento gradual | RC1 com 5 usuários, RC2 com 20–50, 72h sem bloqueadores com evidência de monitoramento. |

Não marcar a meta concluída enquanto algum requisito estiver pendente, sem evidência ou apenas coberto por teste mais restrito.
