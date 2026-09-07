package com.luminor.tavernquest.feature.hero
import androidx.compose.foundation.layout.*;import androidx.compose.material3.Text;import androidx.compose.runtime.Composable;import androidx.compose.ui.Modifier;import com.luminor.tavernquest.core.designsystem.progression.*;import com.luminor.tavernquest.domain.model.HeroProgress
@Composable fun HeroProgressSection(p:HeroProgress){Column{LevelBadge(p.level);XpBar(p.progress,Modifier.fillMaxWidth());Text("XP total: ${p.totalXp}")}}
