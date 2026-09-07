package com.luminor.tavernquest.domain.repository

import com.luminor.tavernquest.domain.model.RankingEntry
import kotlinx.coroutines.flow.Flow

interface RankingRepository {
    fun observeTavern(tavernId: String, periodStart: Long? = null): Flow<List<RankingEntry>>
}
