package com.luminor.tavernquest.data.repository

import com.luminor.tavernquest.data.local.database.dao.ActivityDao
import com.luminor.tavernquest.domain.model.RankingEntry
import com.luminor.tavernquest.domain.repository.RankingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RankingRepositoryImpl(private val dao: ActivityDao) : RankingRepository {
    override fun observeTavern(tavernId: String, periodStart: Long?): Flow<List<RankingEntry>> = dao.observeTavernRanking(tavernId, periodStart ?: 0L).map { rows -> rows.map { RankingEntry(it.heroId, it.xp.coerceIn(0, Int.MAX_VALUE).toInt(), it.activeDays.coerceIn(0, Int.MAX_VALUE).toInt(), it.checkIns.coerceIn(0, Int.MAX_VALUE).toInt()) } }
}
