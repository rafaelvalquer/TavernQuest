package com.luminor.tavernquest.feature.tavern

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun JoinTavernScreen(onDone: () -> Unit, onBack: () -> Unit, vm: JoinTavernViewModel = hiltViewModel()) {
    val state by vm.ui.collectAsState()
    LaunchedEffect(state.joined) { if (state.joined) onDone() }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Entrar em uma Taberna", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(state.code, vm::code, label = { Text("Código de convite") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = vm::join, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text("Entrar") }
        TextButton(onClick = onBack) { Text("Voltar") }
    }
}
