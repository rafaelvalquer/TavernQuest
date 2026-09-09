package com.luminor.tavernquest.feature.onboarding.hero

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luminor.tavernquest.core.designsystem.hero.HeroSprite
import com.luminor.tavernquest.domain.model.HeroAppearance
import com.luminor.tavernquest.domain.model.HeroClass

@Composable
fun HeroClassSelector(selected: HeroClass, onSelect: (HeroClass) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Escolha sua classe", style = MaterialTheme.typography.titleMedium)
        HeroClass.entries.forEach { heroClass ->
            val isSelected = selected == heroClass
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 88.dp)
                    .clickable { onSelect(heroClass) },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    HeroSprite(heroClass, HeroAppearance.MASCULINE, Modifier.size(72.dp))
                    Column {
                        Text(heroClass.title, style = MaterialTheme.typography.titleMedium)
                        Text(heroClassDescription(heroClass), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

private fun heroClassDescription(heroClass: HeroClass) = when (heroClass) {
    HeroClass.WARRIOR -> "Força e disciplina"
    HeroClass.MAGE -> "Sabedoria e estudo"
    HeroClass.RANGER -> "Exploração e movimento"
    HeroClass.ARTISAN -> "Projetos e criação"
    HeroClass.GUARDIAN -> "Família e comunidade"
}
