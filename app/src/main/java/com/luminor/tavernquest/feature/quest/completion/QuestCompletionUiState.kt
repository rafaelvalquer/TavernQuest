package com.luminor.tavernquest.feature.quest.completion
data class QuestCompletionUiState(val title:String="",val notes:String="",val photoPath:String?=null,val saving:Boolean=false,val completed:Boolean=false,val error:String?=null)
