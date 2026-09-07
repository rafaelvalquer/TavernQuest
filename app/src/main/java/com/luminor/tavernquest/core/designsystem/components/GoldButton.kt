package com.luminor.tavernquest.core.designsystem.components
import androidx.compose.material3.*; import androidx.compose.runtime.Composable; import androidx.compose.ui.Modifier; import com.luminor.tavernquest.core.designsystem.theme.Gold
@Composable fun GoldButton(text:String,onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true){ Button(onClick,modifier,enabled,colors=ButtonDefaults.buttonColors(containerColor=Gold)){Text(text)} }
