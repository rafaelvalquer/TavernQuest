package com.luminor.tavernquest.feature.onboarding.tavern
import com.luminor.tavernquest.domain.model.TavernEmblem
data class TavernCreationUiState(val name:String="",val description:String="",val isPrivate:Boolean=true,val emblem:TavernEmblem=TavernEmblem.WOLF,val saving:Boolean=false,val created:Boolean=false)
