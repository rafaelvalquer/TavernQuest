package com.luminor.tavernquest.domain.repository
import com.luminor.tavernquest.domain.model.*; import kotlinx.coroutines.flow.Flow
interface TavernRepository{suspend fun create(tavern:Tavern,owner:TavernMember);suspend fun get():Tavern?;fun observe():Flow<Tavern?>;fun observeForHero(heroId:String):Flow<List<Tavern>>;suspend fun getByCode(code:String):Tavern?;suspend fun join(tavernId:String,member:TavernMember):Boolean;suspend fun leave(tavernId:String,heroId:String)}
