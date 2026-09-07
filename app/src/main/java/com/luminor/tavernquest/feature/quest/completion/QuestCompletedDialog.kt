package com.luminor.tavernquest.feature.quest.completion
import androidx.compose.runtime.Composable;import com.luminor.tavernquest.core.designsystem.components.MedievalDialog
@Composable fun QuestCompletedDialog(onDone:()->Unit)=MedievalDialog("Missão concluída","XP registrado no livro da taberna.",onDone)
