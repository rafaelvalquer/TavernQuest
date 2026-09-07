package com.luminor.tavernquest.domain.model
data class TavernMember(val id:String,val tavernId:String,val heroId:String,val role:TavernRole,val joinedAt:Long)
