package com.luminor.tavernquest.core.designsystem.components
import androidx.compose.material3.*; import androidx.compose.runtime.Composable; import androidx.compose.ui.Modifier
@Composable fun GoldOutlineButton(text:String,onClick:()->Unit,modifier:Modifier=Modifier){ OutlinedButton(onClick,modifier){Text(text)} }
