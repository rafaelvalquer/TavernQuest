package com.luminor.tavernquest.app
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
@Composable fun rememberTavernQuestAppState() = AppState(rememberNavController())
data class AppState(val navController: androidx.navigation.NavHostController)
