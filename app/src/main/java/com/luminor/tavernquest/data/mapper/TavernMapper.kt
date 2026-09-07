package com.luminor.tavernquest.data.mapper
import com.luminor.tavernquest.data.local.database.entity.TavernEntity; import com.luminor.tavernquest.domain.model.*
fun TavernEntity.toDomain()=Tavern(id,name,runCatching { TavernEmblem.valueOf(emblem) }.getOrDefault(TavernEmblem.WOLF),createdAt,description,isPrivate);fun Tavern.toEntity()=TavernEntity(id,name,emblem.name,createdAt,description,isPrivate)
