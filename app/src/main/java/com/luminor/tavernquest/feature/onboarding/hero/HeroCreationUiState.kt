package com.luminor.tavernquest.feature.onboarding.hero
import com.luminor.tavernquest.domain.model.*
data class HeroCreationUiState(val name:String="",val heroClass:HeroClass=HeroClass.WARRIOR,val appearance:HeroAppearance=HeroAppearance.MASCULINE,val saving:Boolean=false,val created:Boolean=false)
