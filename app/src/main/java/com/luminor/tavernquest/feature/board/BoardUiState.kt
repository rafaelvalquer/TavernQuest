package com.luminor.tavernquest.feature.board
import com.luminor.tavernquest.domain.model.*
data class BoardUiState(val loading:Boolean=true,val contracts:List<DailyContract> = emptyList(),val category:ContractCategory?=null,val message:String?=null)
