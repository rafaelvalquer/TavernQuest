package com.luminor.tavernquest.domain.repository

import com.luminor.tavernquest.domain.model.Tavern
import com.luminor.tavernquest.domain.model.TavernMember
import com.luminor.tavernquest.domain.model.CheckIn
import com.luminor.tavernquest.domain.model.RankingEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface TavernRemoteRepository {
    suspend fun create(tavern: Tavern, owner: TavernMember): Result<Tavern>
    suspend fun findByInviteCode(code: String): Result<Tavern?>
    suspend fun join(tavern: Tavern, member: TavernMember): Result<Unit>
    suspend fun leave(tavernId: String): Result<Unit>
    fun observeTaverns(): Flow<List<Tavern>> = flowOf(emptyList())
    fun observeMembers(tavernId: String): Flow<List<TavernMember>> = flowOf(emptyList())
    fun observeFeed(tavernId: String): Flow<List<CheckIn>> = flowOf(emptyList())
    fun observeRanking(tavernId: String, periodStart: Long?): Flow<List<RankingEntry>> = flowOf(emptyList())
}
