package com.luminor.tavernquest.core.designsystem.hero
import androidx.annotation.DrawableRes
import com.luminor.tavernquest.R
import com.luminor.tavernquest.domain.model.*

object HeroSpriteResolver {
    @DrawableRes fun resource(c: HeroClass, appearance: HeroAppearance) = when (c to appearance) {
        HeroClass.WARRIOR to HeroAppearance.MASCULINE -> R.drawable.hero_warrior_male_v2
        HeroClass.WARRIOR to HeroAppearance.FEMININE -> R.drawable.hero_warrior_female_v2
        HeroClass.MAGE to HeroAppearance.MASCULINE -> R.drawable.hero_mage_male_v2
        HeroClass.MAGE to HeroAppearance.FEMININE -> R.drawable.hero_mage_female_v2
        HeroClass.RANGER to HeroAppearance.MASCULINE -> R.drawable.hero_ranger_male_v2
        HeroClass.RANGER to HeroAppearance.FEMININE -> R.drawable.hero_ranger_female_v2
        HeroClass.ARTISAN to HeroAppearance.MASCULINE -> R.drawable.hero_artisan_male_v2
        HeroClass.ARTISAN to HeroAppearance.FEMININE -> R.drawable.hero_artisan_female_v2
        HeroClass.GUARDIAN to HeroAppearance.MASCULINE -> R.drawable.hero_guardian_male_v2
        HeroClass.GUARDIAN to HeroAppearance.FEMININE -> R.drawable.hero_guardian_female_v2
        else -> R.drawable.hero_warrior_male_v2
    }
}
