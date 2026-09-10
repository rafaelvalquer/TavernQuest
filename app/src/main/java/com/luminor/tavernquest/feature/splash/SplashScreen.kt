package com.luminor.tavernquest.feature.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luminor.tavernquest.core.designsystem.components.MedievalLoading
import com.luminor.tavernquest.navigation.AppRoute

@Composable
fun SplashScreen(vm: SplashViewModel = hiltViewModel(), onReady: (String) -> Unit) {
    val state by vm.state.collectAsState()
    LaunchedEffect(state) {
        when (state) {
            SessionBootstrapState.NeedsLogin -> onReady(AppRoute.Welcome)
            SessionBootstrapState.NeedsHero -> onReady(AppRoute.HeroCreation)
            SessionBootstrapState.Ready -> onReady(AppRoute.Home)
            else -> Unit
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val value = state) {
            SessionBootstrapState.Loading -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TavernQuest", fontSize = 34.sp)
                MedievalLoading()
            }
            is SessionBootstrapState.Error -> Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(value.message)
                Button(onClick = vm::retry) { Text("Tentar novamente") }
                Button(onClick = vm::logout) { Text("Sair") }
            }
            else -> Unit
        }
    }
}
