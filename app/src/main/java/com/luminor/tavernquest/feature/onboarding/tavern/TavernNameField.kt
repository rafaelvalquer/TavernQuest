package com.luminor.tavernquest.feature.onboarding.tavern
import androidx.compose.runtime.Composable;import com.luminor.tavernquest.core.designsystem.components.MedievalTextField
@Composable fun TavernNameField(v:String,onChange:(String)->Unit)=MedievalTextField(v,onChange,"Nome da taberna")
