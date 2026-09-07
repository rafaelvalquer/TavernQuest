package com.luminor.tavernquest.feature.onboarding.hero
import androidx.compose.runtime.Composable;import com.luminor.tavernquest.core.designsystem.components.MedievalTextField
@Composable fun HeroNameField(value:String,onChange:(String)->Unit)=MedievalTextField(value,onChange,"Nome do herói")
