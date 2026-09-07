package com.luminor.tavernquest.feature.onboarding.hero
import androidx.compose.foundation.layout.Row;import androidx.compose.material3.*;import androidx.compose.runtime.Composable;import com.luminor.tavernquest.domain.model.HeroAppearance
@Composable fun HeroAppearanceSelector(selected:HeroAppearance,onSelect:(HeroAppearance)->Unit){Row{HeroAppearance.entries.forEach{FilterChip(selected==it,{onSelect(it)},{Text(if(it==HeroAppearance.MASCULINE)"Masculino" else "Feminino")})}}}
