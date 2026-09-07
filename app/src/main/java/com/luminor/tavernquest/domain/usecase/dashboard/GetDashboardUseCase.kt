package com.luminor.tavernquest.domain.usecase.dashboard

import com.luminor.tavernquest.domain.repository.DashboardRepository
import com.luminor.tavernquest.domain.repository.HeroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

data class DashboardSnapshot(val stats: com.luminor.tavernquest.domain.model.DashboardStats, val days: List<com.luminor.tavernquest.domain.model.ActivityDay>)

class GetDashboardUseCase @Inject constructor(private val heroes: HeroRepository, private val dashboard: DashboardRepository) {
    fun observe(from: String, until: String): Flow<DashboardSnapshot> = heroes.observe().flatMapLatest { hero ->
        if (hero == null) emptyFlow() else kotlinx.coroutines.flow.combine(
            dashboard.observeStats(hero.id), dashboard.observeMonth(hero.id, from, until), ::DashboardSnapshot,
        )
    }
}
