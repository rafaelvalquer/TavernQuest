package com.luminor.tavernquest.feature.tavern
import com.luminor.tavernquest.domain.model.*
data class TavernUiState(val hero:Hero?=null,val tavern:Tavern?=null,val active:List<DailyContract> = emptyList(),val progress:HeroProgress?=null)
