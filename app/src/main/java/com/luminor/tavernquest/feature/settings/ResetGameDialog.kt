package com.luminor.tavernquest.feature.settings
import androidx.compose.material3.*;import androidx.compose.runtime.Composable
@Composable fun ResetGameDialog(onConfirm:()->Unit,onDismiss:()->Unit){AlertDialog(onDismissRequest=onDismiss,confirmButton={TextButton(onClick=onConfirm){Text("Apagar")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancelar")}},title={Text("Reiniciar aventura?")},text={Text("Todos os dados locais serão apagados.")})}
