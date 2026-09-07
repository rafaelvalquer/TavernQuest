package com.luminor.tavernquest.core.designsystem.hero
import androidx.compose.material3.Text;import androidx.compose.runtime.Composable;import androidx.compose.ui.unit.sp;import com.luminor.tavernquest.domain.model.*
@Composable fun HeroSprite(heroClass:HeroClass,appearance:HeroAppearance){Text(HeroSpriteResolver.symbol(heroClass),fontSize=72.sp)}
