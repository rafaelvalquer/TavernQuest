package com.luminor.tavernquest.core.designsystem.hero
import com.luminor.tavernquest.domain.model.*
object HeroSpriteResolver{fun symbol(c:HeroClass)=when(c){HeroClass.WARRIOR->"⚔";HeroClass.MAGE->"✦";HeroClass.RANGER->"➶";HeroClass.ARTISAN->"⚒";HeroClass.GUARDIAN->"◆"}}
