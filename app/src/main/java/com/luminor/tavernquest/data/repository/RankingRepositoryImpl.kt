package com.luminor.tavernquest.data.repository

import com.luminor.tavernquest.data.local.database.dao.ActivityDao
import com.luminor.tavernquest.domain.model.RankingEntry
import com.luminor.tavernquest.domain.repository.RankingRepository
import com.luminor.tavernquest.domain.repository.TavernRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class RankingRepositoryImpl(private val dao: ActivityDao, private val remote: TavernRemoteRepository) : RankingRepository {
    override fun observeTavern(tavernId: String, periodStart: Long?): Flow<List<RankingEntry>> = combine(
        dao.observeTavernRanking(tavernId, periodStart ?: 0L).map { rows -> rows.map { RankingEntry(it.heroId, it.xp.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(), it.activeDays.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(), it.checkIns.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()) } },
        remote.observeRanking(tavernId, periodStart),
    ) { local, online -> if (online.isEmpty()) local else online }
}
