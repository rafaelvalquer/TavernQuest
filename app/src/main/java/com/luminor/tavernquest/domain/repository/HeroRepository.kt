package com.luminor.tavernquest.domain.repository
import com.luminor.tavernquest.domain.model.Hero; import kotlinx.coroutines.flow.Flow
interface HeroRepository{suspend fun create(hero:Hero);suspend fun get():Hero?;fun observe():Flow<Hero?>;suspend fun addXp(heroId:String,amount:Int)}
