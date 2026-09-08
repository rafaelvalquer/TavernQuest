package com.luminor.tavernquest.feature.tavern

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luminor.tavernquest.domain.model.CheckIn
import com.luminor.tavernquest.core.util.TavernInviteCode
import android.content.Intent

@Composable
fun TavernDetailScreen(tavernId: String, onBack: () -> Unit, vm: TavernDetailViewModel = hiltViewModel()) {
    LaunchedEffect(tavernId) { vm.load(tavernId) }
    val state by vm.ui.collectAsState()
    LaunchedEffect(state.left) { if (state.left) onBack() }
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text(state.tavern?.name ?: "Taberna", style = MaterialTheme.typography.headlineMedium)
        state.tavern?.description?.takeIf { it.isNotBlank() }?.let { Text(it) }
        Text(if (state.tavern?.isPrivate == false) "Taberna pública" else "Taberna privada")
        Text("${state.memberCount} aventureiros")
        state.tavern?.let { tavern ->
            TextButton(onClick = {
                val code = TavernInviteCode.fromId(tavern.id)
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Entre na Taberna ${tavern.name} no TavernQuest com o código $code.")
                }, "Compartilhar convite"))
            }) { Text("Compartilhar convite") }
        }
        if (state.tavern != null) TextButton(onClick = vm::leave) { Text("Sair da Taberna") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TavernDetailTab.entries.forEach { tab -> FilterChip(selected = state.tab == tab, onClick = { vm.selectTab(tab) }, label = { Text(tab.label()) }) }
        }
        if (state.tab == TavernDetailTab.RANKING) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TavernRankingPeriod.entries.forEach { period -> FilterChip(selected = state.period == period, onClick = { vm.selectPeriod(period) }, label = { Text(period.label()) }) }
            }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (state.tab) {
                TavernDetailTab.FEED -> if (state.feed.isEmpty()) item { Text("Ainda não há check-ins publicados nesta Taberna.") } else items(state.feed, key = { it.id }) { checkIn -> FeedCheckIn(checkIn) }
                TavernDetailTab.RANKING -> if (state.ranking.isEmpty()) item { Text("O ranking ainda está vazio.") } else items(state.ranking, key = { it.heroId }) { entry -> RankingEntryCard(entry) }
                TavernDetailTab.MEMBERS -> if (state.members.isEmpty()) item { Text("Nenhum membro encontrado.") } else items(state.members, key = { it.id }) { member -> MemberCard(member) }
            }
        }
    }
}

private fun TavernDetailTab.label() = when (this) { TavernDetailTab.FEED -> "Feed"; TavernDetailTab.RANKING -> "Ranking"; TavernDetailTab.MEMBERS -> "Membros" }
private fun TavernRankingPeriod.label() = when (this) { TavernRankingPeriod.WEEK -> "Semana"; TavernRankingPeriod.MONTH -> "Mês"; TavernRankingPeriod.ALL -> "Geral" }

@Composable private fun RankingEntryCard(entry: com.luminor.tavernquest.domain.model.RankingEntry) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(entry.heroId, style = MaterialTheme.typography.titleMedium)
        Text("${entry.xp} XP · ${entry.activeDays} dias · ${entry.checkIns} check-ins")
    }
}

@Composable private fun MemberCard(member: com.luminor.tavernquest.domain.model.TavernMember) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(member.heroId, style = MaterialTheme.typography.titleMedium)
        Text(if (member.role == com.luminor.tavernquest.domain.model.TavernRole.OWNER) "Mestre da Taberna" else "Aventureiro")
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
