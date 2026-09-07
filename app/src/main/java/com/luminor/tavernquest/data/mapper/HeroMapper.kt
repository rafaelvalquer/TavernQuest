package com.luminor.tavernquest.data.mapper
import com.luminor.tavernquest.data.local.database.entity.HeroEntity; import com.luminor.tavernquest.domain.model.*
fun HeroEntity.toDomain()=Hero(id,name,HeroAppearance.valueOf(appearance),HeroClass.valueOf(heroClass),totalXp,createdAt); fun Hero.toEntity()=HeroEntity(id,name,appearance.name,heroClass.name,totalXp,createdAt)
