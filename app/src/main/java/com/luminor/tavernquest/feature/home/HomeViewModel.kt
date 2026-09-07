package com.luminor.tavernquest.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.domain.model.*
import com.luminor.tavernquest.domain.repository.HeroRepository
import com.luminor.tavernquest.domain.repository.TavernRepository
import com.luminor.tavernquest.domain.rules.LevelCalculator
import com.luminor.tavernquest.domain.usecase.dashboard.GetDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HomeUiState(val hero: Hero? = null, val stats: DashboardStats = DashboardStats(), val days: List<ActivityDay> = emptyList(), val selectedDate: String? = null, val checkIns: List<CheckIn> = emptyList(), val month: YearMonth = YearMonth.now(), val taverns: List<Tavern> = emptyList())

@HiltViewModel
class HomeViewModel @Inject constructor(private val heroes: HeroRepository, private val dashboard: GetDashboardUseCase, private val level: LevelCalculator, private val tavernRepository: TavernRepository) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow<String?>(null)
    val ui: StateFlow<HomeUiState> = combine(heroes.observe(), month, selectedDate) { hero, current, selected -> Triple(hero, current, selected) }.flatMapLatest { (hero, current, selected) ->
        if (hero == null) emptyFlow<HomeUiState>()
        else combine(dashboard.observe(current.atDay(1).toString(), current.plusMonths(1).atDay(1).toString()), selected?.let { dashboard.observeDay(hero.id, it) } ?: flowOf(emptyList<CheckIn>()), tavernRepository.observeForHero(hero.id)) { snapshot, checkIns, taverns -> HomeUiState(hero, snapshot.stats, snapshot.days, selected, checkIns, current, taverns) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
    fun previousMonth() { month.update { it.minusMonths(1) } }
    fun nextMonth() { month.update { it.plusMonths(1) } }
    fun selectDate(date: String) { selectedDate.value = date }
    fun levelOf(xp: Int) = level.calculate(xp).level
}
