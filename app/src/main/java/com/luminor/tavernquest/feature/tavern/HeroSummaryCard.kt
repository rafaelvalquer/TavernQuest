package com.luminor.tavernquest.feature.tavern
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*;import androidx.compose.material3.Text;import androidx.compose.runtime.Composable;import com.luminor.tavernquest.core.designsystem.components.WoodPanel;import com.luminor.tavernquest.core.designsystem.progression.*;import com.luminor.tavernquest.domain.model.*
@Composable fun HeroSummaryCard(hero:Hero,p:HeroProgress){WoodPanel{Text(hero.name);Text(hero.heroClass.title);LevelBadge(p.level);XpBar(p.progress,Modifier.fillMaxWidth());Text("${p.xpInLevel}/${p.xpForNextLevel} XP")}}
