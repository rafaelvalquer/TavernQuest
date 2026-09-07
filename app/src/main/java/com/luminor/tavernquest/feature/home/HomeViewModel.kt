package com.luminor.tavernquest.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.domain.model.*
import com.luminor.tavernquest.domain.repository.HeroRepository
import com.luminor.tavernquest.domain.rules.LevelCalculator
import com.luminor.tavernquest.domain.usecase.dashboard.GetDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HomeUiState(val hero: Hero? = null, val stats: DashboardStats = DashboardStats(), val days: List<ActivityDay> = emptyList(), val month: YearMonth = YearMonth.now())

@HiltViewModel
class HomeViewModel @Inject constructor(private val heroes: HeroRepository, private val dashboard: GetDashboardUseCase, private val level: LevelCalculator) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    val ui: StateFlow<HomeUiState> = month.flatMapLatest { current ->
        combine(heroes.observe(), dashboard.observe(current.atDay(1).toString(), current.plusMonths(1).atDay(1).toString())) { hero, snapshot -> HomeUiState(hero, snapshot.stats, snapshot.days, current) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
    fun previousMonth() { month.update { it.minusMonths(1) } }
    fun nextMonth() { month.update { it.plusMonths(1) } }
    fun levelOf(xp: Int) = level.calculate(xp).level
}
