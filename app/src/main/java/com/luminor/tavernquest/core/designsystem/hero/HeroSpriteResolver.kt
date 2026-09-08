package com.luminor.tavernquest.core.designsystem.hero
import androidx.annotation.DrawableRes
import com.luminor.tavernquest.R
import com.luminor.tavernquest.domain.model.*

object HeroSpriteResolver {
    @DrawableRes fun resource(c: HeroClass, appearance: HeroAppearance) = when (c to appearance) {
        HeroClass.WARRIOR to HeroAppearance.MASCULINE -> R.drawable.hero_warrior_male
        HeroClass.WARRIOR to HeroAppearance.FEMININE -> R.drawable.hero_warrior_female
        HeroClass.MAGE to HeroAppearance.MASCULINE -> R.drawable.hero_mage_male
        HeroClass.MAGE to HeroAppearance.FEMININE -> R.drawable.hero_mage_female
        HeroClass.RANGER to HeroAppearance.MASCULINE -> R.drawable.hero_ranger_male
        HeroClass.RANGER to HeroAppearance.FEMININE -> R.drawable.hero_ranger_female
        HeroClass.ARTISAN to HeroAppearance.MASCULINE -> R.drawable.hero_artisan_male
        HeroClass.ARTISAN to HeroAppearance.FEMININE -> R.drawable.hero_artisan_female
        HeroClass.GUARDIAN to HeroAppearance.MASCULINE -> R.drawable.hero_guardian_male
        HeroClass.GUARDIAN to HeroAppearance.FEMININE -> R.drawable.hero_guardian_female
        else -> R.drawable.hero_warrior_male
    }
}
