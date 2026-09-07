package com.luminor.tavernquest.feature.onboarding.hero
import androidx.compose.foundation.layout.*;import androidx.compose.material3.*;import androidx.compose.runtime.Composable;import com.luminor.tavernquest.domain.model.HeroClass
@Composable fun HeroClassSelector(selected:HeroClass,onSelect:(HeroClass)->Unit){Column{Text("Classe");HeroClass.entries.forEach{FilterChip(selected==it,{onSelect(it)},{Text(it.title)})}}}
