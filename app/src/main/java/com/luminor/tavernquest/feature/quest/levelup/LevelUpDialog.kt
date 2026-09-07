package com.luminor.tavernquest.feature.quest.levelup
import androidx.compose.runtime.Composable;import com.luminor.tavernquest.core.designsystem.components.MedievalDialog
@Composable fun LevelUpDialog(level:Int,onDismiss:()->Unit)=MedievalDialog("Subiu de nível!","Você chegou ao nível $level.",onDismiss)
