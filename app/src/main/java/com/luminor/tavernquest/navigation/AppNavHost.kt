package com.luminor.tavernquest.navigation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.luminor.tavernquest.feature.splash.SplashScreen
import com.luminor.tavernquest.feature.onboarding.welcome.WelcomeScreen
import com.luminor.tavernquest.feature.onboarding.hero.HeroCreationScreen
import com.luminor.tavernquest.feature.onboarding.tavern.TavernCreationScreen
 import com.luminor.tavernquest.feature.tavern.TavernScreen
 import com.luminor.tavernquest.feature.tavern.TavernListScreen
import com.luminor.tavernquest.feature.home.HomeScreen
import com.luminor.tavernquest.feature.board.ContractBoardScreen
import com.luminor.tavernquest.feature.quest.detail.QuestDetailScreen
import com.luminor.tavernquest.feature.quest.completion.QuestCompletionScreen
import com.luminor.tavernquest.feature.journal.JournalScreen
import com.luminor.tavernquest.feature.journal.CompletionDetailScreen
import com.luminor.tavernquest.feature.hero.HeroScreen
import com.luminor.tavernquest.feature.ranking.RankingScreen
import com.luminor.tavernquest.feature.settings.SettingsScreen
@Composable fun AppNavHost(nav:NavHostController, modifier:Modifier=Modifier){ NavHost(nav,AppRoute.Splash,modifier){
 composable(AppRoute.Splash){ SplashScreen{ route->nav.navigateAndClear(route) } }
 composable(AppRoute.Welcome){ WelcomeScreen{ nav.navigate(AppRoute.HeroCreation) } }
 composable(AppRoute.HeroCreation){ HeroCreationScreen{ nav.navigate(AppRoute.TavernCreation) } }
 composable(AppRoute.TavernCreation){ TavernCreationScreen{ nav.navigateAndClear(AppRoute.Home) } }
 composable(AppRoute.Home){ HomeScreen(onOpenBoard={nav.navigate(AppRoute.Board)}) }
 composable(AppRoute.Tavern){ TavernScreen(onOpenBoard={nav.navigate(AppRoute.Board)},onQuest={nav.navigate(AppRoute.quest(it))}) }
 composable(AppRoute.Taverns){ TavernListScreen() }
 composable(AppRoute.Board){ ContractBoardScreen(onQuest={nav.navigate(AppRoute.quest(it))}) }
 composable(AppRoute.Journal){ JournalScreen() }
 composable(AppRoute.Ranking){ RankingScreen() }
 composable(AppRoute.Hero){ HeroScreen() }
 composable(AppRoute.Settings){ SettingsScreen() }
 composable(AppRoute.QuestDetail, listOf(navArgument("id"){type=NavType.StringType})){ QuestDetailScreen(onBack={nav.popBackStack()},onComplete={nav.navigate(AppRoute.complete(it))}) }
 composable(AppRoute.QuestCompletion, listOf(navArgument("id"){type=NavType.StringType})){ QuestCompletionScreen(onDone={nav.navigateAndClear(AppRoute.Home)},onBack={nav.popBackStack()}) }
 composable(AppRoute.CompletionDetail){ CompletionDetailScreen() }
 } }
