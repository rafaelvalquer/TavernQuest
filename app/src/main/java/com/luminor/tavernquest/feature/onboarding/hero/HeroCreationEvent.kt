package com.luminor.tavernquest.feature.onboarding.hero
sealed interface HeroCreationEvent{data class Name(val value:String):HeroCreationEvent;data object Save:HeroCreationEvent}
