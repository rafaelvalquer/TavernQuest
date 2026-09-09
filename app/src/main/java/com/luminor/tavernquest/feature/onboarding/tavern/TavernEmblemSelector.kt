package com.luminor.tavernquest.feature.onboarding.tavern

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.luminor.tavernquest.R
import com.luminor.tavernquest.domain.model.TavernEmblem

@Composable
fun TavernEmblemSelector(selected: TavernEmblem, onSelect: (TavernEmblem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Escolha o brasão", style = MaterialTheme.typography.titleMedium)
        TavernEmblem.entries.forEach { emblem ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (selected == emblem) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier.clickable { onSelect(emblem) },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(painterResource(emblem.resourceId()), contentDescription = emblem.title, modifier = Modifier.size(48.dp))
                    Text(emblem.title, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

private fun TavernEmblem.resourceId() = when (this) {
    TavernEmblem.WOLF -> R.drawable.emblem_wolf
    TavernEmblem.DRAGON -> R.drawable.emblem_dragon
    TavernEmblem.BEAR -> R.drawable.emblem_bear
    TavernEmblem.EAGLE -> R.drawable.emblem_eagle
    TavernEmblem.DEER -> R.drawable.emblem_deer
    TavernEmblem.PHOENIX -> R.drawable.emblem_phoenix
    TavernEmblem.CROWN -> R.drawable.emblem_crown
    TavernEmblem.SHIELD -> R.drawable.emblem_shield
}
