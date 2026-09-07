package com.luminor.tavernquest.feature.ranking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RankingScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ranking", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Text("O ranking da Taberna será atualizado a partir dos check-ins validados.")
        Text("Nenhum ranking publicado neste dispositivo ainda.")
    }
}
