package com.luminor.tavernquest.feature.quest.detail
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luminor.tavernquest.core.designsystem.components.GoldButton
import com.luminor.tavernquest.core.designsystem.components.GoldOutlineButton
import com.luminor.tavernquest.domain.model.ContractStatus
import kotlinx.coroutines.delay

@Composable
fun QuestDetailScreen(vm: QuestDetailViewModel = hiltViewModel(), onBack: () -> Unit, onComplete: (String) -> Unit) {
    val s by vm.ui.collectAsState()
    val q = s.quest
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(q?.startedAt) {
        while (q?.startedAt != null && q.completedAt == null) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (q == null) Text(if (s.loading) "Carregando missão..." else "Missão não encontrada.")
        else {
            Text(q.template.title)
            Text(q.template.description)
            Text(q.template.category.title)
            Text("Recompensa: ${q.template.xpReward} XP")
            val seconds = q.startedAt?.let { ((q.completedAt ?: now) - it).coerceAtLeast(0) / 1000 } ?: 0
            Text("%02d:%02d:%02d".format(seconds / 3600, seconds / 60 % 60, seconds % 60))
            s.message?.let { Text(it) }
            when (q.status) {
                ContractStatus.AVAILABLE -> GoldButton("Aceitar missão", vm::accept)
                ContractStatus.ACCEPTED -> if (q.startedAt == null) GoldButton("Iniciar missão", vm::start)
                    else { Text("Em curso"); GoldButton("Concluir", { onComplete(q.id) }) }
                ContractStatus.COMPLETED -> Text("Check-in registrado")
                else -> Text("Missão encerrada")
            }
        }
        GoldOutlineButton("Voltar", onBack)
    }
}
