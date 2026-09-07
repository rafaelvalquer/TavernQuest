package com.luminor.tavernquest.domain.repository
import com.luminor.tavernquest.domain.model.*; import kotlinx.coroutines.flow.Flow
interface TavernRepository{suspend fun create(tavern:Tavern,owner:TavernMember);suspend fun get():Tavern?;fun observe():Flow<Tavern?>}
