package com.luminor.tavernquest.core.designsystem.components
import androidx.compose.material3.*; import androidx.compose.runtime.Composable
@Composable fun MedievalDialog(title:String,text:String,onDismiss:()->Unit){ AlertDialog(onDismissRequest=onDismiss,confirmButton={TextButton(onClick=onDismiss){Text("OK")}},title={Text(title)},text={Text(text)}) }
