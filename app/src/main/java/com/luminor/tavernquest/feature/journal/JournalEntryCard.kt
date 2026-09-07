package com.luminor.tavernquest.feature.journal
import androidx.compose.material3.Text;import androidx.compose.runtime.Composable;import com.luminor.tavernquest.core.designsystem.components.ParchmentCard;import com.luminor.tavernquest.core.designsystem.theme.Ink;import com.luminor.tavernquest.domain.model.QuestCompletion
@Composable fun JournalEntryCard(e:QuestCompletion){ParchmentCard{Text("Missão concluída",color=Ink);Text(e.notes.ifBlank{"Sem anotações"},color=Ink);Text(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(e.completedAt)),color=Ink)}}
