package com.luminor.tavernquest.core.designsystem.hero
import androidx.compose.runtime.Composable;import com.luminor.tavernquest.domain.model.*
@Composable fun HeroPortrait(hero:Hero){HeroSprite(hero.heroClass,hero.appearance)}
