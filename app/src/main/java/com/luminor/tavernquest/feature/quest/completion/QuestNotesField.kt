package com.luminor.tavernquest.feature.quest.completion
import androidx.compose.runtime.Composable;import com.luminor.tavernquest.core.designsystem.components.MedievalTextField
@Composable fun QuestNotesField(v:String,onChange:(String)->Unit)=MedievalTextField(v,onChange,"Como foi a missão?",singleLine=false)
