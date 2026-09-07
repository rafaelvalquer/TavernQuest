package com.luminor.tavernquest.core.designsystem.progression
import androidx.compose.material3.*;import androidx.compose.runtime.Composable;import androidx.compose.ui.Modifier
@Composable fun XpBar(progress:Float,modifier:Modifier=Modifier){LinearProgressIndicator({progress},modifier)}
