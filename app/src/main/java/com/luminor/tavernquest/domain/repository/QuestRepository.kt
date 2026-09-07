package com.luminor.tavernquest.domain.repository
interface QuestRepository {
    suspend fun start(contractId: String, heroId: String): Boolean
    suspend fun complete(contractId:String,heroId:String,notes:String,photoPath:String?):Boolean
}
