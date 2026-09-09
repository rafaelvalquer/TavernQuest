package com.luminor.tavernquest.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luminor.tavernquest.core.designsystem.hero.HeroSprite
import com.luminor.tavernquest.core.designsystem.progression.LevelBadge
import com.luminor.tavernquest.domain.model.ActivityDay
import java.time.format.DateTimeFormatter
import java.util.Locale
import android.graphics.BitmapFactory
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun HomeScreen(onOpenBoard: () -> Unit, vm: HomeViewModel = hiltViewModel()) {
    val state by vm.ui.collectAsState()
    val progress = vm.progressOf(state.stats.totalXp)
    val byDate = state.days.associateBy { it.date }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Início", style = MaterialTheme.typography.headlineMedium)
        state.hero?.let { hero ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                HeroSprite(hero.heroClass, hero.appearance, Modifier.size(96.dp))
            }
            Text(hero.name, style = MaterialTheme.typography.titleLarge)
            Text(hero.heroClass.title)
            LevelBadge(vm.levelOf(state.stats.totalXp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { DashboardStat(state.stats.totalCheckIns.toString(), "Missões"); DashboardStat(state.stats.activeDays.toString(), "Dias ativos"); DashboardStat(formatDuration(state.stats.activeSeconds), "Tempo ativo") }
        Text("🔥 Sequência atual: ${state.stats.currentStreak} dias · recorde: ${state.stats.longestStreak}")
        LinearProgressIndicator(progress = { progress.progress }, modifier = Modifier.fillMaxWidth())
        Text("${progress.xpInLevel} / ${progress.xpForNextLevel} XP")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { TextButton(onClick = vm::previousMonth) { Text("‹") }; Text(state.month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR")))); TextButton(onClick = vm::nextMonth) { Text("›") } }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) { listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB").forEach { Text(it, style = MaterialTheme.typography.labelSmall) } }
        LazyVerticalGrid(columns = GridCells.Fixed(7), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            items(state.month.atDay(1).dayOfWeek.value % 7) { Box(Modifier.aspectRatio(1f)) }
            items(state.month.lengthOfMonth()) { index -> val day = index + 1; val date = state.month.atDay(day).toString(); CheckInDay(day, byDate[date], state.selectedDate == date) { vm.selectDate(date) } }
        }
        if (state.selectedDate != null) {
            Text("Check-ins de ${state.selectedDate}", style = MaterialTheme.typography.titleMedium)
            if (state.checkIns.isEmpty()) Text("Nenhuma atividade registrada neste dia.")
            state.checkIns.forEach { checkIn -> Text("${checkIn.title} · +${checkIn.xpEarned} XP · ${formatDuration(checkIn.durationSeconds)}") }
        }
        Text("Suas Tabernas", style = MaterialTheme.typography.titleMedium)
        if (state.taverns.isEmpty()) Text("Você ainda não participa de nenhuma Taberna.")
        state.taverns.forEach { tavern -> Text("${tavern.emblem.title} ${tavern.name}") }
        Button(onClick = onOpenBoard, modifier = Modifier.fillMaxWidth()) { Text("+ Nova missão") }
    }
}
@Composable private fun DashboardStat(value: String, label: String) { Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) { Text(value, style = MaterialTheme.typography.titleLarge); Text(label, style = MaterialTheme.typography.labelSmall) } }
@Composable private fun CheckInDay(day: Int, activity: ActivityDay?, selected: Boolean, onClick: () -> Unit) {
    val thumbnail = remember(activity?.thumbnailPath) { activity?.thumbnailPath?.let { path -> BitmapFactory.decodeFile(path)?.asImageBitmap() } }
    androidx.compose.material3.Surface(onClick = onClick, color = if (selected) MaterialTheme.colorScheme.secondaryContainer else if (activity == null) Color.Transparent else MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.aspectRatio(1f)) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
            if (thumbnail != null) Image(thumbnail, contentDescription = "Foto do check-in", modifier = Modifier.fillMaxSize())
            Text(if (activity == null) day.toString() else "$day\n⚔${activity.count}")
        }
    }
}
private fun formatDuration(seconds: Long): String = if (seconds < 3600) "${seconds / 60}min" else "${seconds / 3600}h ${(seconds % 3600) / 60}min"
