package com.luminor.tavernquest.domain.repository
import com.luminor.tavernquest.domain.model.*; import kotlinx.coroutines.flow.Flow
interface ContractRepository{suspend fun ensureSeeded();suspend fun generateBoard(date:String,heroId:String);suspend fun getBoard(date:String):List<DailyContract>;fun observeBoard(date:String):Flow<List<DailyContract>>;suspend fun get(id:String):DailyContract?;suspend fun accept(id:String,date:String):Boolean;suspend fun abandon(id:String);suspend fun expireBefore(date:String)}
