package com.luminor.tavernquest.feature.onboarding.hero

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luminor.tavernquest.core.designsystem.hero.HeroSprite
import com.luminor.tavernquest.domain.model.HeroAppearance
import com.luminor.tavernquest.domain.model.HeroClass

@Composable
fun HeroPreview(c: HeroClass, a: HeroAppearance, modifier: Modifier = Modifier) =
    HeroSprite(c, a, modifier.size(192.dp))
