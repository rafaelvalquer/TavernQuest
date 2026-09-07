package com.luminor.tavernquest.feature.tavern

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luminor.tavernquest.core.util.TavernInviteCode

@Composable
fun TavernListScreen(onJoin: () -> Unit = {}, onCreate: () -> Unit = {}, onOpen: (String) -> Unit = {}, vm: TavernListViewModel = hiltViewModel()) {
    val taverns by vm.taverns.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Suas Tabernas", style = MaterialTheme.typography.headlineMedium)
        if (taverns.isEmpty()) Text("Você ainda não participa de nenhuma Taberna.")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onCreate, modifier = Modifier.weight(1f)) { Text("Criar") }
            OutlinedButton(onClick = onJoin, modifier = Modifier.weight(1f)) { Text("Entrar") }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(taverns, key = { it.id }) { tavern -> Card(onClick = { onOpen(tavern.id) }, modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(tavern.name, style = MaterialTheme.typography.titleLarge); tavern.description.takeIf { it.isNotBlank() }?.let { Text(it) }; Text(if (tavern.isPrivate) "Privada · código: ${TavernInviteCode.fromId(tavern.id)}" else "Pública") } } } }
    }
}
