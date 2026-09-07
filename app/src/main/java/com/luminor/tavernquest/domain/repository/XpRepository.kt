package com.luminor.tavernquest.domain.repository
import kotlinx.coroutines.flow.Flow
interface XpRepository{fun observeTotal(heroId:String):Flow<Int>;suspend fun total(heroId:String):Int}
