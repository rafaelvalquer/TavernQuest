package com.luminor.tavernquest.core.designsystem.components
import androidx.compose.material3.*; import androidx.compose.runtime.Composable
@Composable fun SealButton(text:String,onClick:()->Unit){ FilledTonalButton(onClick){Text(text)} }
