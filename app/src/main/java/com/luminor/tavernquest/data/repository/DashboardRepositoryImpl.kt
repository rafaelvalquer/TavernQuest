package com.luminor.tavernquest.data.repository

import com.luminor.tavernquest.data.local.database.dao.ActivityDao
import com.luminor.tavernquest.data.mapper.toCheckIn
import com.luminor.tavernquest.domain.model.*
import com.luminor.tavernquest.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DashboardRepositoryImpl(private val dao: ActivityDao) : DashboardRepository {
    override fun observeStats(heroId: String): Flow<DashboardStats> = dao.observeStats(heroId).map {
        DashboardStats(it?.totalCheckIns ?: 0, it?.totalXp ?: 0, it?.activeDays ?: 0, it?.activeSeconds ?: 0)
    }
    override fun observeMonth(heroId: String, from: String, until: String): Flow<List<ActivityDay>> = dao.observeMonth(heroId, from, until).map { days ->
        days.map { ActivityDay(it.date, it.checkInCount, it.totalXp, it.activeSeconds, it.primaryCategory?.let { value -> runCatching { ContractCategory.valueOf(value) }.getOrNull() }) }
    }
    override fun observeDay(heroId: String, date: String): Flow<List<CheckIn>> = dao.observeDay(heroId, date).map { values -> values.map { it.toCheckIn() } }
}
