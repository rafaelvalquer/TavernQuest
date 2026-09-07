package com.luminor.tavernquest.feature.ranking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RankingScreen(vm: RankingViewModel = hiltViewModel()) {
    val state by vm.ui.collectAsState()
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ranking", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Text(state.tavernName ?: "Nenhuma Taberna selecionada")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { RankingPeriod.entries.forEach { period -> androidx.compose.material3.FilterChip(selected = state.period == period, onClick = { vm.select(period) }, label = { Text(period.name) }) } }
        if (state.entries.isEmpty()) Text("Nenhum check-in validado no período.")
        state.entries.forEachIndexed { index, entry -> Text("${index + 1}. ${entry.heroId} — ${entry.xp} XP · ${entry.checkIns} check-ins · ${entry.activeDays} dias") }
    }
}
