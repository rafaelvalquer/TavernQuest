package com.luminor.tavernquest.feature.onboarding.tavern

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luminor.tavernquest.core.designsystem.components.GoldButton
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TavernChoiceScreen(onCreate: () -> Unit, onJoin: () -> Unit, onSkip: () -> Unit, vm: TavernChoiceViewModel = hiltViewModel()) {
    val state = vm.completed.collectAsState()
    LaunchedEffect(state.value) { if (state.value) onSkip() }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Escolha sua Taberna")
        Text("Funde um novo grupo ou entre em uma aventura existente com um código de convite.")
        GoldButton("Fundar Taberna", onCreate, modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = onJoin, modifier = Modifier.fillMaxWidth()) { Text("Entrar com código") }
        OutlinedButton(onClick = vm::skip, modifier = Modifier.fillMaxWidth()) { Text("Continuar sem Taberna") }
    }
}
