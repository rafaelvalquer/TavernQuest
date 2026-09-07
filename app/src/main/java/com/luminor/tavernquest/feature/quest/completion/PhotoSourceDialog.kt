package com.luminor.tavernquest.feature.quest.completion
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PhotoSourceDialog(onGallery: () -> Unit, onDismiss: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(androidx.compose.ui.unit.dp(8))) {
        Button(onClick = onGallery) { Text("Galeria") }
        Button(onClick = onDismiss) { Text("Cancelar") }
    }
}
