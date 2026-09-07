package com.luminor.tavernquest.feature.settings
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*;import androidx.compose.material3.*;import androidx.compose.runtime.Composable
@Composable fun SettingsToggle(label:String,checked:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label);Switch(checked,onChange)}}
