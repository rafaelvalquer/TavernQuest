1. Nova visão do TavernQuest

O conceito passa a ser:

Gymrats + RPG medieval + missões pessoais + competição entre amigos.

A unidade central deixa de ser apenas a “missão concluída” e passa a ser o:

Check-in de aventura

Fluxo principal:

Jogador
   ↓
Escolhe uma missão
   ↓
Inicia a missão
   ↓
Conclui
   ↓
CHECK-IN
   ├── XP
   ├── duração
   ├── foto opcional
   ├── observação
   └── data/hora
         ↓
    Perfil do jogador
         ↓
 ┌───────┼────────┐
 ↓       ↓        ↓
Taberna A Taberna B Taberna C
 ↓       ↓        ↓
Feed    Feed      Feed
 ↓       ↓        ↓
Ranking Ranking   Ranking

O ponto importante é: o check-in não será duplicado.

Existe apenas um check-in verdadeiro.

As Tabernas recebem uma referência/publicação desse check-in.

Isso evita:

XP duplicado;
informações divergentes;
várias fotos iguais;
dificuldade para editar;
inconsistência no ranking.
2. Regra principal dos check-ins

Quando Rafael concluir uma missão:

Missão: Caminhar 5 km
XP: +80
Tempo: 52 min
Check-in: #ABC123

e participar das Tabernas:

⚔ Guerreiros da Manhã
🐺 Alcateia
🔥 Projeto 90 Dias

o mesmo check-in será publicado automaticamente nos três feeds.

O XP pessoal aumenta apenas:

+80 XP

e não:

+240 XP

Porém, para o ranking de cada Taberna, aqueles 80 XP passam a contar normalmente.

Esse será um princípio estrutural do sistema.

3. Regra de entrada e saída das Tabernas

Recomendo adotar desde o início esta regra:

Entrou na Taberna hoje
        ↓
Check-ins anteriores NÃO entram no ranking
        ↓
Somente check-ins posteriores ao joinedAt

Assim ninguém entra em uma Taberna já trazendo milhares de XP antigos para assumir o primeiro lugar.

Ao sair:

Check-ins futuros → não contam mais

Check-ins anteriores → permanecem no histórico
4. Mudança importante no modelo atual

Há uma incompatibilidade estrutural no projeto atual.

Hoje DailyContract contém:

val tavernId: String

ou seja, a missão diária está vinculada diretamente a uma Taberna.

Para o novo conceito isso deve mudar.

Uma missão pertence ao jogador, não à Taberna.

Passaremos conceitualmente de:

Taberna
  ↓
Missão
  ↓
Conclusão

para:

Jogador
  ↓
Missão
  ↓
Check-in
  ↓
Tabernas

Isso é fundamental para permitir que um check-in apareça em vários grupos.

5. Outra mudança necessária: múltiplas Tabernas

O banco atual já possui TavernMember com:

tavernId
heroId
role
joinedAt

o que é uma boa base para multiplayer.

Mas o DAO atual está programado para retornar apenas uma Taberna:

SELECT * FROM tavern LIMIT 1

tanto em getTavern() quanto em observeTavern().

Isso será substituído por:

observeTavernsForHero(heroId)
getTavern(tavernId)
getMemberships(heroId)
createTavern(...)
joinTavern(...)
leaveTavern(...)

O jogador poderá, por exemplo, fazer parte de:

Minhas Tabernas

🐺 Os Lobos                  #2
🔥 Projeto Verão             #1
🛡 Família Valquer            #3
⚔ Academia                   #5
6. Dashboard será a nova tela inicial

A tela mostrada na referência do Gymrats é uma boa base de hierarquia de informação, mas faremos uma identidade totalmente TavernQuest.

A atual TavernScreen é bastante simples: cabeçalho, resumo do herói, resumo do mural e contratos ativos.

Ela deixará de ser a home.

A nova rota será:

HOME / DASHBOARD

Visualmente:

╔══════════════════════════════════════╗
║              ⚙                     ║
║                                      ║
║            ╔════════╗                ║
║            ║ SPRITE ║                ║
║            ╚════════╝                ║
║              RAFAEL                  ║
║         Guerreiro • Nível 7          ║
║                                      ║
║   ⚔ 42           🔥 18        ⏳ 14h  ║
║  Missões      Dias ativos     Ativo  ║
║                                      ║
║ ██████████████░░░░  1.850 / 2.000 XP║
║                                      ║
║          SETEMBRO DE 2026             ║
║                                      ║
║ Dom Seg Ter Qua Qui Sex Sáb           ║
║          1   2   3   4   5            ║
║  6  [⚔] 8   9  10  11  12            ║
║ 13  14 [🔥] 16 17  18  19            ║
║ 20  21  22  23 24 [📜] 26            ║
║ 27  28  29  30                       ║
║                                      ║
║       Ver todos os check-ins          ║
║                                      ║
║ ────────────────────────────────────  ║
║ Suas Tabernas                         ║
║ 🐺 Alcateia                   #2      ║
║ 🔥 Projeto 90 Dias            #1      ║
╚══════════════════════════════════════╝
7. Avatar no dashboard

O círculo simples do Gymrats vira algo mais imersivo.

Exemplo:

       ╔═══════════╗
      ║  🧙 SPRITE ║
       ╚═══════════╝
          RAFAEL
       Mago • Nv. 8

Com moldura medieval baseada no nível.

Por exemplo:

Nv 1–4     Madeira
Nv 5–9     Ferro
Nv 10–19   Bronze
Nv 20–29   Prata
Nv 30+     Ouro

Isso cria recompensa visual sem mexer na mecânica do personagem.

8. As três estatísticas principais

Exatamente seguindo a hierarquia da referência:

Estatística	Significado
Missões	quantidade total de check-ins
Dias ativos	quantidade de dias únicos com pelo menos 1 check-in
Tempo ativo	soma da duração das missões concluídas

Exemplo:

       266                255               16h 32m
      Missões          Dias ativos         Tempo ativo

Eu acrescentaria o XP logo abaixo, e não como uma quarta estatística:

Nível 12
████████████████░░░░
7.320 / 8.000 XP
9. Precisamos começar a registrar duração

Hoje o modelo de conclusão possui:

id
dailyContractId
heroId
notes
proofPhotoPath
completedAt

e não possui duração.

Vamos acrescentar ao check-in:

data class CheckIn(
    val id: String,
    val heroId: String,
    val missionId: String,

    val title: String,
    val category: ContractCategory,

    val xpEarned: Int,

    val startedAt: Long?,
    val completedAt: Long,
    val durationSeconds: Long,

    val notes: String?,
    val proofPhotoUrl: String?,

    val syncStatus: SyncStatus
)
10. Timer de missão

No detalhe da missão:

╔══════════════════════════════╗
║       CAMINHAR 5 KM          ║
║                              ║
║          +80 XP              ║
║                              ║
║         00:00:00             ║
║                              ║
║       [ INICIAR MISSÃO ]      ║
╚══════════════════════════════╝

Depois:

         00:37:24

          EM CURSO

        [ CONCLUIR ]

Ao concluir:

Missão concluída!

+80 XP
37 min ativos

Adicionar foto
Adicionar comentário

[FAZER CHECK-IN]

Não precisamos manter um cronômetro rodando continuamente em memória.

Salvamos:

startedAt

e calculamos:

agora - startedAt

Isso continua funcionando mesmo se o Android encerrar o app.

11. Calendário de check-ins

Essa será uma das partes mais importantes da Home.

Criaria:

CheckInCalendar.kt
MonthlyCheckInCalendar.kt
CheckInDay.kt
CalendarHeader.kt
CheckInDayIndicator.kt

Um dia sem atividade:

12

Um dia com uma missão:

[⚔]

Com duas:

[⚔2]

Com várias:

[🔥4]

Também poderemos mostrar uma pequena miniatura da foto de comprovação, similar ao comportamento da referência enviada.

Ao tocar no dia:

15 DE SETEMBRO

⚔ Treino de força
+100 XP
01h 05min
18:43

📚 Ler 30 páginas
+50 XP
00h 41min
21:20

XP do dia
150 XP
12. O botão de Check-in

Podemos manter a sensação do botão + do Gymrats, mas substituir por algo do universo do TavernQuest.

Por exemplo:

      [ ⚔ ]

ou:

   [ + MISSÃO ]

Ele abre imediatamente o Mural.

Outra opção ainda mais temática:

        📜
    NOVA MISSÃO

como um selo flutuante.

13. Tabernas passam a ser os grupos

Nova tela:

TABERNAS

Exemplo:

╔═════════════════════════════════╗
║ SUAS TABERNAS                   ║
║                                 ║
║ 🐺 ALCATEIA                     ║
║ 12 aventureiros                 ║
║ Você está em #2                 ║
║                                 ║
║ 🔥 PROJETO 90 DIAS              ║
║ 8 aventureiros                  ║
║ Você está em #1                 ║
║                                 ║
║        + Criar Taberna          ║
║        Entrar com código        ║
╚═════════════════════════════════╝
14. Criar Taberna

Fluxo:

Criar Taberna
      ↓
Nome
      ↓
Brasão
      ↓
Descrição
      ↓
Privacidade
      ↓
Criar

Depois:

Taberna criada!

Código:
X7K9Q2

[Compartilhar convite]

Não colocaria busca pública de grupos no primeiro MVP social.

Inicialmente:

Taberna privada
+
código de convite

é suficiente.

15. Entrar em uma Taberna
Entrar na Taberna

Digite o código

[ X7K9Q2 ]

[ ENTRAR ]

Depois:

🐺 ALCATEIA

12 membros

Você deseja entrar?

[ENTRAR NA TABERNA]
16. Dentro de uma Taberna

A Taberna terá três seções principais:

TABERNA
│
├── Feed
├── Ranking
└── Membros

Tela:

🐺 ALCATEIA
12 aventureiros

[ Feed ] [ Ranking ] [ Membros ]

──────────────────────────

Rafael
Guerreiro • Nível 8

⚔ Caminhada de 5 km

+80 XP
52 min

[foto]

Hoje • 11:32

──────────────────────────

Isabela
Maga • Nível 5

📚 Leitura por 30 minutos

+50 XP
31 min

Esse será o equivalente ao feed social do Gymrats, mas como um “mural da guilda”.

17. Ranking da Taberna

Essa é uma das funcionalidades centrais.

🏆 RANKING
🐺 Alcateia

[Semana] [Mês] [Geral]

🥇 João
   1.840 XP

🥈 Rafael
   1.720 XP

🥉 Amanda
   1.480 XP

4  Carlos
   1.220 XP

Filtros:

Semana
Mês
Geral

Depois podemos adicionar:

Temporada
18. Regra do ranking

Ranking principal:

SUM(XP obtido em check-ins)

Não é baseado somente em quantidade de atividades.

Isso preserva a lógica RPG.

Exemplo:

João
10 missões
1.000 XP

Rafael
8 missões
1.250 XP

Resultado:

1 Rafael  1.250 XP
2 João    1.000 XP

Em empate:

1. maior XP
2. maior quantidade de dias ativos
3. maior quantidade de check-ins
19. Uma regra importante contra abuso

O sistema atual já possui um ledger de XP com índice único por:

sourceType + sourceId

o que impede o mesmo contrato de gerar XP duas vezes.

E a conclusão atual já executa check-in, XP e alteração de status dentro de uma transação Room.

Essa lógica deve ser mantida e ampliada.

No multiplayer:

Missão
  ↓
Check-in
  ↓
XP Transaction
  ↓
Atualização Ranking

serão logicamente uma única operação.

20. Não permitir XP calculado pelo celular

Quando o aplicativo tiver competição real, não é recomendável fazer:

xp = userInput

nem confiar apenas no Android para dizer:

Ganhei 500 XP

O aplicativo envia:

missionId
startedAt
completedAt
photo
notes

E o backend verifica:

missionId
↓
XP oficial da missão
↓
limite diário
↓
se já foi concluída
↓
gera check-in
↓
concede XP

Isso reduz bastante manipulação do ranking.

21. Backend necessário

O TavernQuest atual não possui nenhuma dependência cloud; o Gradle contém Compose, Room, DataStore, Hilt e Coroutines.

Para grupos reais, recomendo:

Firebase

Usaremos:

Serviço	Função
Firebase Authentication	contas
Cloud Firestore	Tabernas, membros, check-ins
Firebase Storage	fotos
Cloud Functions	XP/ranking seguro
Firebase Cloud Messaging	notificações
Crashlytics	erros
Analytics	métricas opcionais

O Android continuará:

100% Kotlin
Jetpack Compose
Room
Hilt

Firebase será apenas a infraestrutura online.

22. Arquitetura offline-first

Não recomendo abandonar Room.

A arquitetura ficará:

UI Compose
      ↓
ViewModel
      ↓
UseCase
      ↓
Repository
     ↙   ↘
 Room   Firebase
 local   remoto

Assim o usuário poderá abrir:

perfil;
calendário;
histórico;
mural já carregado;

mesmo offline.

23. Check-in offline

Caso esteja sem sinal:

CONCLUIR MISSÃO
       ↓
Room
       ↓
Check-in:
PENDING_SYNC
       ↓
UI mostra imediatamente
       ↓
Internet voltou
       ↓
WorkManager
       ↓
Firebase
       ↓
VALIDATED

Até o servidor validar:

XP local: provisório
Ranking: ainda não publicado

Quando validado:

SYNCED
24. Modelo remoto

Estrutura sugerida no Firebase:

users/
   {userId}

userStats/
   {userId}

missions/
   {missionId}

checkins/
   {checkInId}

taverns/
   {tavernId}
      members/
         {userId}

      feed/
         {checkInId}

      leaderboards/
         week-2026-W37/
             entries/
                 {userId}

         month-2026-09/
             entries/
                 {userId}

         all/
             entries/
                 {userId}

invites/
   {inviteCode}
25. Check-in global

Documento:

checkins/{checkInId}

Campos:

id
userId
missionId

missionTitle
category
difficulty

xpEarned

startedAt
completedAt
durationSeconds

notes
photoUrl

createdAt
26. Fan-out para Tabernas

Depois de criar:

checkins/ABC

o backend verifica:

membership do jogador

Se estiver em:

Tavern A
Tavern B
Tavern C

cria:

TavernA/feed/ABC
TavernB/feed/ABC
TavernC/feed/ABC

São referências/espelhos leves.

Não novos check-ins.

27. Estatísticas agregadas

Não devemos ficar lendo milhares de check-ins toda vez que a Home abrir.

Teremos:

UserStats(
    totalCheckIns,
    totalXp,
    activeDays,
    activeSeconds,
    currentStreak,
    longestStreak
)

Então o dashboard carrega instantaneamente:

266 Missões
255 Dias ativos
16h32 Ativo
28. Estatística por dia

Para o calendário:

user_activity_day

Modelo:

UserActivityDay(
    date: LocalDate,
    checkInCount: Int,
    totalXp: Int,
    activeSeconds: Long,
    primaryCategory: ContractCategory?,
    thumbnailUrl: String?
)

Isso permite carregar um mês inteiro de forma extremamente leve.

29. Remodelagem do Hero

Hoje o Hero contém apenas:

id
name
appearance
heroClass
totalXp
createdAt

Precisamos evoluir para algo como:

data class Hero(
    val id: String,
    val userId: String,

    val name: String,

    val heroClass: HeroClass,
    val appearance: HeroAppearance,

    val avatarId: String,
    val avatarType: AvatarType,

    val profilePhotoUrl: String?,

    val totalXp: Int,
    val createdAt: Long
)

Assim teremos tanto:

Sprite

quanto opcionalmente:

Foto real
30. Criação de conta muito mais imersiva

Hoje a tela basicamente apresenta título, preview, nome, aparência e classe.

Vamos transformar isso em uma sequência narrativa.

Splash
 ↓
Welcome
 ↓
Criar conta
 ↓
Nome do aventureiro
 ↓
Escolher classe
 ↓
Escolher aparência
 ↓
Escolher sprite
 ↓
Preview final
 ↓
Criar/entrar em uma Taberna
 ↓
Tutorial
 ↓
Dashboard
31. Tela de boas-vindas

Em vez de tela simples:

[imagem de uma taberna medieval]

TAVERNQUEST

Toda jornada começa
com uma pequena missão.

[COMEÇAR A AVENTURA]

Já possuo uma conta

Com:

fogo da lareira;
taberna pixel-art;
silhuetas de heróis;
pequenas animações.
32. Escolha de classe

Cinco cards grandes:

⚔ GUERREIRO
Força e disciplina

🧙 MAGO
Sabedoria e estudo

🏹 PATRULHEIRO
Exploração e movimento

⚒ ARTESÃO
Projetos e criação

🛡 GUARDIÃO
Família e comunidade

Cada card terá uma ilustração/sprite verdadeiro.

33. Atualmente não existem sprites reais ligados ao código

O HeroSpriteResolver atual retorna apenas símbolos:

⚔
✦
➶
⚒
◆

em vez de resolver imagens.

Vamos substituir por:

HeroSpriteCatalog

Exemplo:

HeroClass.WARRIOR + MASCULINE
    -> hero_warrior_01

HeroClass.WARRIOR + FEMININE
    -> hero_warrior_02
34. Pacote inicial de arte

Para o primeiro release social eu faria:

Arte	Quantidade
Sprites de heróis	10
Ilustrações das classes	5
Fundos de onboarding	3
Fundos de Taberna	3
Brasões	8
Runas de categorias	5
Molduras de avatar	5
Selos de check-in	5
Troféus/ranking	3

Todos seguindo o mesmo estilo:

pixel art medieval
+
fantasia acolhedora
+
taberna
+
madeira
+
pergaminho
+
ouro
35. Preview do personagem

Durante a criação:

╔══════════════════════╗
║                      ║
║       [SPRITE]       ║
║                      ║
║       RAFAEL         ║
║     GUERREIRO        ║
║                      ║
╚══════════════════════╝

       ◀  1 / 3  ▶

Mudou classe?

O cenário e o sprite mudam imediatamente.

36. Novo bottom navigation

Hoje existem quatro destinos principais:

Taberna
Mural
Diário
Herói

Eu alteraria para:

🏠 Início
📜 Missões
🍺 Tabernas
🏆 Ranking
🧙 Herói

O Diário não desaparece.

Ele passa para:

Home
   ↓
Calendário
   ↓
Ver todos os check-ins

Isso combina melhor com o novo produto.

37. Nova navegação

Estrutura aproximada:

Splash

Auth
├── Login
└── Register

Onboarding
├── HeroName
├── HeroClass
├── HeroAppearance
├── HeroSprite
├── HeroPreview
└── TavernChoice

Main
├── Home
├── Missions
├── Taverns
├── Ranking
└── Hero

CheckIn
├── MissionDetail
├── ActiveMission
├── CompleteMission
└── CheckInDetail

Tavern
├── TavernList
├── TavernDetail
├── CreateTavern
├── JoinTavern
├── Members
└── Invite

History
├── Calendar
├── DayCheckIns
└── CheckInDetail

Settings
38. Organização de pastas nova

Eu manteria o projeto em um único módulo app; não há necessidade de fragmentar Gradle neste momento.

A estrutura principal passaria para:

com.luminor.tavernquest/

app/

navigation/

core/
├── designsystem/
├── media/
├── time/
├── network/
├── sync/
└── util/

domain/
├── model/
├── repository/
├── rules/
└── usecase/
    ├── auth/
    ├── mission/
    ├── checkin/
    ├── dashboard/
    ├── tavern/
    ├── ranking/
    └── hero/

data/
├── local/
│   ├── database/
│   └── preferences/
│
├── remote/
│   └── firebase/
│       ├── auth/
│       ├── firestore/
│       ├── storage/
│       └── dto/
│
├── mapper/
├── repository/
└── sync/

feature/
├── auth/
├── onboarding/
├── home/
├── mission/
├── checkin/
├── tavern/
├── ranking/
├── history/
├── hero/
└── settings/
39. Novos componentes Compose

Especialmente para o visual:

feature/home/
├── HomeScreen.kt
├── HomeViewModel.kt
├── HomeUiState.kt
├── HeroDashboardHeader.kt
├── DashboardStatRow.kt
├── DashboardStat.kt
├── MonthlyCheckInCalendar.kt
├── CheckInCalendarDay.kt
├── XpDashboardBar.kt
├── TavernQuickRanking.kt
└── RecentCheckIns.kt

Taberna:

feature/tavern/
├── list/
├── detail/
├── create/
├── join/
├── invite/
├── members/
└── components/
    ├── TavernCard.kt
    ├── TavernFeed.kt
    ├── CheckInFeedCard.kt
    └── TavernMemberAvatar.kt

Ranking:

feature/ranking/
├── RankingScreen.kt
├── RankingViewModel.kt
├── RankingUiState.kt
├── RankingPeriodSelector.kt
├── RankingPodium.kt
├── RankingEntry.kt
└── CurrentHeroRank.kt
40. Novo banco Room

A versão atual é version = 1 e possui sete entidades principais.

A evolução deverá usar:

version = 2

Eu não usaria fallbackToDestructiveMigration().

Criaria:

MIGRATION_1_2

para preservar dados de quem já usa o app.

41. Novas entidades locais

Teremos aproximadamente:

HeroEntity
TavernEntity
TavernMemberEntity

ContractTemplateEntity
DailyMissionEntity

CheckInEntity
XpLedgerEntity

UserStatsEntity
UserActivityDayEntity

TavernLeaderboardEntity
TavernFeedEntity

PendingSyncEntity
42. Repository interfaces novas
AuthRepository
HeroRepository
MissionRepository
CheckInRepository
TavernRepository
RankingRepository
DashboardRepository
SyncRepository
SettingsRepository

Isso mantém a Clean Architecture atual e permite trocar Firebase no futuro sem reescrever telas.

43. Use cases importantes
RegisterUserUseCase
LoginUseCase

CreateHeroUseCase
SelectAvatarUseCase

GetDashboardUseCase
GetMonthActivityUseCase

StartMissionUseCase
CompleteMissionUseCase
CreateCheckInUseCase

CreateTavernUseCase
JoinTavernUseCase
LeaveTavernUseCase
GetUserTavernsUseCase

GetTavernFeedUseCase
GetTavernRankingUseCase

SyncPendingCheckInsUseCase
44. Ranking em tempo real

Para não recalcular milhares de registros:

quando houver:

+80 XP

a função atualiza:

ranking semanal +80
ranking mensal +80
ranking geral +80

por Taberna.

Exemplo:

week-2026-W37
month-2026-09
all

Assim abrir o ranking custa poucas leituras e responde rapidamente.

45. Notificações

Depois do núcleo social pronto:

🔥 João acabou de ganhar 100 XP

🏆 Rafael assumiu o 1º lugar na Alcateia

⚔ Amanda ultrapassou você por 40 XP

🍺 Carlos entrou na sua Taberna

🔥 Sua chama está em risco hoje

Isso tende a ser uma das mecânicas mais fortes de retenção desse tipo de app.

46. Reações aos check-ins

Eu deixaria fora da primeira implementação, mas já prepararia o modelo para:

🔥
⚔
👏
🍺
👑

Depois cada feed poderá mostrar:

🔥 4   ⚔ 2   👏 7

Isso reforça a parte social sem precisar construir chat.

47. Não faria chat agora

Não colocaria neste MVP:

mensagens privadas;
chat de grupo;
comentários em árvore;
busca global de pessoas;
grupos públicos;
seguidores.

O principal loop deve ser:

MISSÃO
  ↓
CHECK-IN
  ↓
XP
  ↓
RANKING
  ↓
COMPETIÇÃO
  ↓
NOVA MISSÃO

Esse loop precisa ficar excelente antes de aumentar o escopo.

48. Roadmap de implementação

Eu dividiria o desenvolvimento assim:

Etapa	Objetivo
1	estabilizar projeto atual e garantir build limpo
2	remodelar missão → check-in
3	implementar Dashboard novo
4	calendário e estatísticas
5	novo onboarding com sprites
6	Firebase Authentication
7	transformar Taberna em múltiplos grupos
8	criação/entrada/convites
9	sincronização dos check-ins
10	feed das Tabernas
11	ranking semanal/mensal/geral
12	foto remota / Firebase Storage
13	offline-first + WorkManager
14	segurança/anti-XP duplicado
15	notificações
16	testes e polimento visual
49. Ordem que eu usaria na prática

Há uma razão para não começar pelo multiplayer imediatamente.

Primeiro eu faria o aplicativo parecer e funcionar como o produto final em modo solo:

Novo onboarding
     ↓
Novo personagem
     ↓
Dashboard
     ↓
Calendário
     ↓
Missão
     ↓
Timer
     ↓
Check-in
     ↓
XP

Quando esse loop estiver bom:

Firebase
   ↓
Conta
   ↓
Tabernas
   ↓
Membros
   ↓
Feed
   ↓
Ranking

Isso reduz muito a chance de tentar depurar UI, Room, Firebase, autenticação e ranking simultaneamente.

50. Resultado final esperado

O TavernQuest deixará de parecer apenas um “gerenciador de hábitos com tema medieval”.

O produto passa a ter uma identidade muito mais clara:

Um aplicativo social onde a vida real vira uma aventura RPG.

A pessoa abre e vê:

Seu herói
Seu progresso
Seus dias ativos
Seu calendário
Seus check-ins
Seu nível

cumpre algo na vida real:

Treino
Estudo
Corrida
Projeto
Família

faz:

CHECK-IN

recebe:

XP

e imediatamente vê:

🐺 Alcateia
Rafael subiu de #4 para #2

Essa combinação de histórico pessoal + calendário + personagem + grupo + competição + XP é, na minha avaliação do código atual, uma evolução muito mais forte para o TavernQuest do que simplesmente adicionar mais tipos de missões.

A base atual já possui duas partes especialmente valiosas para essa evolução: o modelo de membros já separa tavernId de heroId, e o ledger de XP já foi pensado para evitar recompensa duplicada. O maior refactor será transformar a arquitetura de “uma Taberna local com missões vinculadas a ela” em “um jogador com check-ins globais participando de várias Tabernas”.