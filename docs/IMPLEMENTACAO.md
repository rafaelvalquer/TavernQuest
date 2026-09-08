# Evolução social — acompanhamento

Escopo original integral: [PLANO_SOCIAL.md](PLANO_SOCIAL.md). Build verde não prova conclusão do produto.

| Etapas do plano | Situação / evidência necessária |
| --- | --- |
| 1. Estabilização | Build debug passou após correção dos retornos e imports. Wrapper personalizado ainda precisa correção. |
| 2. Missão pessoal, check-in global | Migrações não destrutivas v1→v7, timer persistente, snapshot, sequências, feed por referência, ID de missão oficial e transação única implementados. Testes Room cobrem duplicidade, migração, reconciliação, rejeição e fan-out local. |
| 3–4. Dashboard e calendário | Dashboard offline com estatísticas agregadas, sequência atual/recorde, navegação mensal, indicadores por dia, miniatura local de foto e detalhe diário com XP/duração implementado. |
| 5. Onboarding e arte | Sequência local de herói, escolha entre fundar, entrar ou continuar sem Taberna e catálogo de sprites reais implementados; pacote completo de arte e tutorial narrativo ainda pendentes. |
| 6. Contas Firebase | Telas de login/cadastro, repositório Firebase Auth e vínculo `Hero.userId` implementados. O projeto `tavernquest-7684e`, Authentication e Firestore em `southamerica-east1` estão provisionados; falta colocar o `google-services.json` baixado em `app/` e implantar Functions após habilitar faturamento. |
| 7–8. Múltiplas Tabernas | Repositório local observa várias Tabernas, permite entrar/sair pela interface, lista grupos e oferece entrada por código curto determinístico. A fundação captura descrição e privacidade, e o detalhe reúne feed, ranking por período, membros e compartilhamento de convite. Criação publica um convite remoto resolvível por outro dispositivo; entrada e saída também publicam a associação no Firestore quando há autenticação. O modo local continua funcionando sem Firebase. |
| 9–11. Sync, feed e ranking | Feed local por Taberna implementado como referência para o check-in global, com fan-out para todas as associações ativas na mesma transação Room. Ranking local por Taberna usa joinedAt, XP, dias ativos e check-ins. Function remota versionada para validação, fan-out, userStats e acumuladores semanal/mensal/geral; deploy remoto ainda pendente. |
| 12–13. Fotos e offline | Seleção de foto da galeria, cópia para armazenamento privado local, fila Room, WorkManager com rede disponível, transporte Firestore, upload Storage, correção do XP oficial e rollback de rejeição em transação Room implementados. Storage e Cloud Functions exigem habilitar faturamento no projeto Firebase antes da publicação online. |
| 14. Segurança | Rules e Functions versionadas; XP oficial, duração, status, catálogo oficial, lock idempotente por usuário/missão e fan-out validados no servidor. Deploy, autenticação real e isolamento online ainda pendentes. |
| 15. Notificações | Serviço Android registra tokens FCM no perfil e exibe notificações; Functions remotas avisam após validação do check-in e quando um aventureiro entra na Taberna. Permissão Android foi declarada. |
| 16. Testes e visual | Testes instrumentados de check-in/migração, reconciliação de XP, rejeição e código de convite adicionados. Fluxos completos e duas contas online ainda pendentes. |

Também verificar: cinco destinos principais, Hero ampliado, interfaces/use cases do plano, Crashlytics, métricas opcionais, migração de dados existentes. Reações somente preparadas no modelo; chat e busca pública ficam fora conforme itens 46–47. Funcionalidade social real não deve ser substituída por simulação local.
