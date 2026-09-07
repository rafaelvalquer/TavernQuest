package com.luminor.tavernquest.core.designsystem.hero
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import com.luminor.tavernquest.domain.model.*

@Composable fun HeroSprite(heroClass: HeroClass, appearance: HeroAppearance, modifier: Modifier = Modifier) {
    Image(painterResource(HeroSpriteResolver.resource(heroClass, appearance)), contentDescription = "Sprite de ${heroClass.title}", contentScale = ContentScale.Fit, modifier = modifier)
}
