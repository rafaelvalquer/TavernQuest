package com.luminor.tavernquest.domain.repository

import com.luminor.tavernquest.domain.model.ActivityDay
import com.luminor.tavernquest.domain.model.DashboardStats
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun observeStats(heroId: String): Flow<DashboardStats>
    fun observeMonth(heroId: String, from: String, until: String): Flow<List<ActivityDay>>
}
