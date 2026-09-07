package com.luminor.tavernquest.app
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import com.luminor.tavernquest.core.designsystem.components.MedievalBottomBar
import com.luminor.tavernquest.navigation.AppNavHost
import com.luminor.tavernquest.navigation.MainDestination
@Composable fun TavernQuestApp(state: AppState = rememberTavernQuestAppState()) {
 val route = state.navController.currentBackStackEntryAsState().value?.destination?.route
 val showBottom = MainDestination.entries.any { route == it.route }
 Scaffold(bottomBar={ if(showBottom) MedievalBottomBar(route){ dest -> state.navController.navigate(dest.route){ launchSingleTop=true; restoreState=true } } }) { pad ->
   AppNavHost(state.navController, Modifier.padding(pad))
 }
}
