# Evolução social — acompanhamento

Escopo original integral: [PLANO_SOCIAL.md](PLANO_SOCIAL.md). Build verde não prova conclusão do produto.

| Etapas do plano | Situação / evidência necessária |
| --- | --- |
| 1. Estabilização | Build debug passou após correção dos retornos e imports. Wrapper personalizado ainda precisa correção. |
| 2. Missão pessoal, check-in global | Em implementação: migração não destrutiva, timer persistente, snapshot e transação única. Exigir testes reais Room, duplicidade e migração. |
| 3–4. Dashboard e calendário | Dashboard offline com estatísticas agregadas, navegação mensal, indicadores por dia e detalhe diário com XP/duração implementado. Miniaturas ainda pendentes. |
| 5. Onboarding e arte | Pendente: sequência narrativa, catálogo real e pacote completo de arte (item 34). |
| 6. Contas Firebase | Regras Firestore/Storage, `firebase.json` e Function de validação/fan-out versionados. Login, cadastro, sessão e projeto real ainda dependem do console. |
| 7–8. Múltiplas Tabernas | Repositório local observa várias Tabernas, permite entrar/sair, lista grupos e oferece entrada por código curto determinístico. Convites remotos e compartilhamento pendentes. |
| 9–11. Sync, feed e ranking | Feed local por Taberna implementado como referência para o check-in global, com fan-out para todas as associações ativas na mesma transação Room. Ranking local por Taberna usa joinedAt, XP, dias ativos e check-ins. Function remota versionada para validação, fan-out, userStats e acumuladores semanal/mensal/geral; deploy remoto ainda pendente. |
| 12–13. Fotos e offline | Fila Room, estados de sincronização, correção do XP oficial e rollback de rejeição em transação Room implementados. Storage, WorkManager e cliente remoto ainda pendentes. |
| 14. Segurança | Rules e Functions versionadas; XP oficial, duração, status e fan-out validados no servidor. Deploy, autenticação real e isolamento online ainda pendentes. |
| 15. Notificações | Function remota envia FCM após validação do check-in quando há tokens registrados. Permissão Android, registro de token e demais eventos ainda pendentes. |
| 16. Testes e visual | Testes instrumentados de check-in/migração, reconciliação de XP, rejeição e código de convite adicionados. Fluxos completos e duas contas online ainda pendentes. |

Também verificar: cinco destinos principais, Hero ampliado, interfaces/use cases do plano, Crashlytics, métricas opcionais, migração de dados existentes. Reações somente preparadas no modelo; chat e busca pública ficam fora conforme itens 46–47. Funcionalidade social real não deve ser substituída por simulação local.
