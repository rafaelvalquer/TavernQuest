package com.luminor.tavernquest.domain.repository
interface QuestRepository{suspend fun complete(contractId:String,heroId:String,notes:String,photoPath:String?):Boolean}
