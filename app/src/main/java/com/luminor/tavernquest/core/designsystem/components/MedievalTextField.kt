package com.luminor.tavernquest.core.designsystem.components
import androidx.compose.material3.*; import androidx.compose.runtime.Composable; import androidx.compose.ui.Modifier
@Composable fun MedievalTextField(value:String,onValueChange:(String)->Unit,label:String,modifier:Modifier=Modifier,singleLine:Boolean=true){ OutlinedTextField(value,onValueChange,modifier,label={Text(label)},singleLine=singleLine) }
