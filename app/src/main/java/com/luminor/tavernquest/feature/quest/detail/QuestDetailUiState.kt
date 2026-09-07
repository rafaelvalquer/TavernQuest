package com.luminor.tavernquest.feature.quest.detail
import com.luminor.tavernquest.domain.model.DailyContract
data class QuestDetailUiState(val quest:DailyContract?=null,val loading:Boolean=true,val accepted:Boolean=false,val message:String?=null)
