package com.luminor.tavernquest.domain.model
data class Tavern(val id:String,val name:String,val emblem:TavernEmblem,val createdAt:Long,val description:String="",val isPrivate:Boolean=true)
