package com.luminor.tavernquest.feature.hero
import androidx.compose.material3.Text;import androidx.compose.runtime.Composable;import com.luminor.tavernquest.domain.model.Hero
@Composable fun HeroHeader(h:Hero){Text("${h.name} • ${h.heroClass.title}")}
