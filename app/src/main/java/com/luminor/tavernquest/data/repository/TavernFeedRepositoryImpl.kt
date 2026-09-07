package com.luminor.tavernquest.data.repository

import com.luminor.tavernquest.data.local.database.dao.TavernFeedDao
import com.luminor.tavernquest.data.mapper.toCheckIn
import com.luminor.tavernquest.domain.model.CheckIn
import com.luminor.tavernquest.domain.repository.TavernFeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TavernFeedRepositoryImpl(private val dao: TavernFeedDao) : TavernFeedRepository {
    override fun observe(tavernId: String): Flow<List<CheckIn>> = dao.observe(tavernId).map { values -> values.map { it.toCheckIn() } }
}
