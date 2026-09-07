package com.luminor.tavernquest.feature.tavern

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TavernListScreen(vm: TavernListViewModel = hiltViewModel()) {
    val taverns by vm.taverns.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Suas Tabernas", style = MaterialTheme.typography.headlineMedium)
        if (taverns.isEmpty()) Text("Você ainda não participa de nenhuma Taberna.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(taverns, key = { it.id }) { tavern -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(tavern.name, style = MaterialTheme.typography.titleLarge); Text("Código de convite: ${tavern.id}") } } } }
    }
}
