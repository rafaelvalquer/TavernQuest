# Evolução social — acompanhamento

Escopo original integral: [PLANO_SOCIAL.md](PLANO_SOCIAL.md). Build verde não prova conclusão do produto.

| Etapas do plano | Situação / evidência necessária |
| --- | --- |
| 1. Estabilização | Build debug passou após correção dos retornos e imports. Wrapper personalizado ainda precisa correção. |
| 2. Missão pessoal, check-in global | Em implementação: migração não destrutiva, timer persistente, snapshot e transação única. Exigir testes reais Room, duplicidade e migração. |
| 3–4. Dashboard e calendário | Pendente: estatísticas agregadas, navegação mensal, detalhe do dia, histórico e fotos. |
| 5. Onboarding e arte | Pendente: sequência narrativa, catálogo real e pacote completo de arte (item 34). |
| 6. Contas Firebase | Pendente: login, cadastro, sessão e configuração do projeto. |
| 7–8. Múltiplas Tabernas | Pendente: criar, prévia de convite, entrar, sair, membros e compartilhamento. |
| 9–11. Sync, feed e ranking | Pendente: backend autoritativo, fan-out de referências, joinedAt/saída, desempates e períodos. |
| 12–13. Fotos e offline | Pendente: Storage, WorkManager, retry idempotente e validação/rejeição de XP provisório. |
| 14. Segurança | Pendente: Rules e Functions, limites oficiais, teste concorrente e isolamento de contas. |
| 15. Notificações | Pendente: FCM, permissões, preferências e eventos do item 45. |
| 16. Testes e visual | Pendente: testes significativos (há placeholders), emulador, fluxos completos e duas contas online. |

Também verificar: cinco destinos principais, Hero ampliado, interfaces/use cases do plano, Crashlytics, métricas opcionais, migração de dados existentes. Reações somente preparadas no modelo; chat e busca pública ficam fora conforme itens 46–47. Funcionalidade social real não deve ser substituída por simulação local.
