package com.luminor.tavernquest.feature.tavern

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luminor.tavernquest.domain.model.CheckIn

@Composable
fun TavernDetailScreen(tavernId: String, onBack: () -> Unit, vm: TavernDetailViewModel = hiltViewModel()) {
    LaunchedEffect(tavernId) { vm.load(tavernId) }
    val state by vm.ui.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text(state.tavern?.name ?: "Taberna", style = MaterialTheme.typography.headlineMedium)
        Text("${state.memberCount} aventureiros")
        Text("Mural da guilda", style = MaterialTheme.typography.titleMedium)
        if (state.feed.isEmpty()) Text("Ainda não há check-ins publicados nesta Taberna.")
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.feed, key = { it.id }) { checkIn -> FeedCheckIn(checkIn) }
        }
    }
}

@Composable
private fun FeedCheckIn(checkIn: CheckIn) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(checkIn.title, style = MaterialTheme.typography.titleMedium)
        Text("${checkIn.xpEarned} XP · ${checkIn.durationSeconds / 60} min")
        if (!checkIn.notes.isNullOrBlank()) Text(checkIn.notes)
    }
}
