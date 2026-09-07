package com.luminor.tavernquest.domain.repository

import com.luminor.tavernquest.domain.model.CheckIn
import kotlinx.coroutines.flow.Flow

interface TavernFeedRepository {
    fun observe(tavernId: String): Flow<List<CheckIn>>
}
