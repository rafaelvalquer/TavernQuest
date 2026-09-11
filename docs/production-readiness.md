# Preparação para distribuição por APK

Estado em 11/09/2026: **lançamento público ainda não aprovado**. O canal inicial é instalação direta, sem Google Play. Este registro acompanha o plano de produção; uma compilação bem-sucedida não substitui os critérios abaixo.

## Evidências locais confirmadas

- `scripts/build-production.ps1` concluiu lintProdRelease, testProdReleaseUnitTest e assembleProdRelease, verificou assinatura e correspondência do certificado com o cliente OAuth Android no JSON de produção.
- APK exportado: `build/distribution/TavernQuest-1.0.1-2-prodRelease.apk`.
- SHA-256 do arquivo exportado: `e4e0423d1d9374aa3b869590866144d9c417555ddc2feef878c33b208bbd9321` (build de diagnóstico concluído; ajustes posteriores de mensagens ainda não estão neste APK).
- Pacote: `com.luminor.tavernquest`; projeto configurado: `tavernquest-7684e`; certificado SHA-1: `A0:0B:BE:35:D5:C9:AA:98:7D:EC:B5:0F:D8:05:FC:62:6C:86:1A:CC`.
- compileSdk/targetSdk 36. Provider App Check debug separado por source set e dependência debugImplementation.
- Telemetria de autenticação sanitizada; não anexa mensagem ou causa original contendo dados de conta. Erros de login possuem classificação e referência técnica.
- Lint completo passou após declarar câmera opcional. Permanecem avisos a revisar; não foi aplicado baseline para ocultar erros.
- Firebase Emulator: 9 testes passaram (5 Firestore e 4 Storage), sem falhas. Cobrem isolamento de perfil, check-in pendente, acesso de membro à taberna, bloqueio de escrita de estado de missão, propriedade/tipo/tamanho/caminho de fotos e bloqueio de exclusão pelo cliente. Não cobrem todas as Functions nem App Check.
- Room: os 3 testes instrumentados de migração passaram no Pixel_8 AVD Android 17: 1→2, 1→7 e 6→7. A matriz das demais origens e APIs continua pendente.
- ResetGameTest passou no mesmo emulador: limpeza real do Room iniciada na Main, sem permitir operações de banco nessa thread, seguida por reset de preferências e logout. Correção aplicada também à limpeza em SettingsViewModel.
- Handlers de conta/taberna: 6 testes passaram contra Firestore Emulator (autenticação ausente, concorrência/idempotência, perfil incompleto/inválido, entrada/saída em taberna, formato de convite e limites concorrentes com expiração). ensureUserAccount e bootstrapProfile agora usam transações e validam classe/aparência. ensureUserAccount e bootstrapProfile publicadas com sucesso em tavernquest-7684e/southamerica-east1. Ambas responderam HTTP 401 UNAUTHENTICATED a uma chamada sem credenciais. Os testes de concorrência chamam os handlers diretamente; login com token real e App Check continuam pendentes.

Essas evidências se referem ao estado local e ao APK identificado acima. Não comprovam que o celular do usuário recebeu esse arquivo ou que as configurações remotas continuam corretas.

## Condições ainda pendentes

| Área | Trabalho e evidência exigida |
| --- | --- |
| Login real | 10/10 tentativas no release, em pelo menos 3 celulares e 2 contas; login, logout, retorno e limpeza de dados. Usuário ainda reporta falha em APK de variante desconhecida e está sem USB. |
| Firebase Console | Confirmar Google ativo, configuração OAuth/certificados atuais e decisão pendente sobre desativação de e-mail/senha. |
| Sessão | Testes de integração do bootstrap, falha/retry e perfil incompleto; comprovar users/{uid} e navegação remota. |
| CI | Workflow alterado para clean/lint/test/build prodRelease com chave descartável e Firebase fictício, sem segredos de produção. Gerador prod/dev validado localmente. Matriz de Room/cache configurada nas APIs 26/29/31/33/34/35/36. Faltam análise Kotlin, Functions e execução confirmada no GitHub. |
| Proteção Git | main somente por PR com checks obrigatórios; verificar estado remoto. |
| Release por tag | Build assinado com segredos protegidos, versão/tag consistentes e artifact + SHA-256; não fornecer chave de produção a PR não confiável. |
| Room | Testes 1→2, 1→7, 2→7 e 6→7 passaram no emulador. Recuperar schemas 3/4/5 com origem verificável e testar cada versão histórica até 7 preservando dados. |
| Firebase Emulator | Firestore + Storage: 9 testes passaram; handlers de Functions: 6 testes passaram. Ampliar cobertura das demais operações e autorizações. |
| App Check | Configurar Play Integrity para APK direto, comprovar atestação em release físico e só depois ativar enforcement em Functions/Firestore/Storage. |
| Segurança servidor | Revisar autenticação, parâmetros, limites, anti-spam, convites, XP, recompensas, feed e ranking. |
| Exclusão de conta | UI com confirmação/reauth, operação remota idempotente, remoção de dados e identidade, testes e canal externo de solicitação. |
| Privacidade | Política pública acessível no app, contato e retenção definidos, descrição de Auth/fotos/Analytics/Crashlytics/FCM. |
| Apresentação | Ícone adaptativo e roundIcon vetoriais próprios implementados; recursos compilaram. Falta inspeção no launcher e revisão de splash/nome/acabamento. |
| R8 | Minificação e redução de recursos habilitadas no release. Primeiro build otimizado aprovado (lint, unitários, assinatura e mapping Crashlytics), APK com 24.080.647 bytes; testar o APK resultante em Auth, Firebase, Hilt, Room, WorkManager, FCM e Crashlytics antes de distribuir. |
| Compatibilidade | APIs 26/29/31/33/34/35/36; notificações, câmera/foto/FileProvider, trabalho em background e permissões negadas. |
| Aceitação funcional | Instalação/upgrade, herói, taberna em duas contas, missões/conclusão/XP/streak, fotos/sync, offline/retorno, push, reinício/troca de conta, erros, App Check/Crashlytics/exclusão. |
| Lançamento gradual | RC1 com 5 usuários, RC2 com 20–50, 72h sem bloqueadores com evidência de monitoramento. |

Não marcar a meta concluída enquanto algum requisito estiver pendente, sem evidência ou apenas coberto por teste mais restrito.




## Atualização de 11/09

createTavern, joinTavern e resolveInvite tiveram publicação concluída com sucesso. A revisão publicada inclui entrada transacional e validação do código. Limites por usuário e bloqueio da leitura direta de convites passaram em seis testes de handlers e nove testes de regras e tiveram deploy concluído em produção. O release com R8 instalou e abriu no emulador sem crash, mas o bootstrap de uma sessão existente exibiu recuperação de perfil; isso permanece um bloqueador de aceitação em investigação. Houve erro de atestação App Check no emulador, sem prova de que ele seja a causa do bootstrap.

Os logs de ensureUserAccount de 11/09 às 15:32 UTC registraram autenticação VALID e App Check INVALID, com a chamada permitida porque enforcement estava desativado. O build de diagnóstico concluiu lint, unitários e assinatura e foi instalado no emulador. Às 16:13 UTC, o Logcat registrou ACCOUNT_SETUP:q:UNAVAILABLE; o mapping R8 identifica q como FirebaseFirestoreException. O SDK também registrou que o backend Firestore não respondeu em 10 segundos. Isso identifica a falha observada como indisponibilidade de leitura Firestore após a Function; não comprova a causa da conectividade nem o erro do aparelho físico. O tratamento por código UNAVAILABLE/DEADLINE_EXCEEDED e um teste de regressão foram adicionados e aguardam validação. O teste instrumentado da origem Room 2→7 está em execução.

Na mesma instalação, um toque em Tentar novamente concluiu o bootstrap e abriu o Dashboard com perfil e taberna existentes, confirmado pela hierarquia de UI do emulador. Isso comprova recuperação com sessão existente nesse aparelho virtual; não equivale a novo login Google nem à matriz de celulares físicos.

Migration2To7Test concluiu no Pixel_8 API 37: 1 teste, 0 falhas, 0 erros. A fixture nasce do schema 2 exportado e verifica perfil/XP, missão, duração, foto, fila de envio com tentativas, atividade diária e feed após migração até 7. O CI agora preserva relatórios Android/Room por 14 dias; execução remota ainda não verificada.

Nova validação concluída: 26 testes unitários prodRelease sem falhas e FirestoreFailureMessageTest passou no Android API 37. O teste da exceção Firestore usa Android porque o SDK inicializa SparseArray; a tentativa anterior em JVM simples falhou por ausência desse runtime, corrigida ao mover a regressão para androidTest.

A revisão da sincronização identificou que reference.get() anterior ao primeiro envio era negado para check-ins inexistentes. As regras agora permitem a consulta de ausência por usuário autenticado e mantêm documentos existentes restritos ao dono. Dez testes de regras (6 Firestore + 4 Storage) passaram, incluindo ausência, criação, releitura do dono e rejeição de outra conta. A publicação dessa correção foi iniciada e aguarda confirmação terminal.
