package com.luminor.tavernquest.feature.hero
import com.luminor.tavernquest.domain.model.*
data class HeroUiState(val hero:Hero?=null,val progress:HeroProgress?=null,val streak:Int=0)
