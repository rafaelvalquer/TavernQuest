package com.luminor.tavernquest.feature.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.domain.model.RankingEntry
import com.luminor.tavernquest.domain.repository.RankingRepository
import com.luminor.tavernquest.domain.repository.TavernRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class RankingPeriod { WEEK, MONTH, ALL }

data class RankingUiState(val period: RankingPeriod = RankingPeriod.WEEK, val entries: List<RankingEntry> = emptyList(), val tavernName: String? = null)

@HiltViewModel
class RankingViewModel @Inject constructor(private val taverns: TavernRepository, private val ranking: RankingRepository) : ViewModel() {
    private val period = MutableStateFlow(RankingPeriod.WEEK)
    val ui: StateFlow<RankingUiState> = combine(taverns.observe(), period) { tavern, selected -> tavern to selected }.flatMapLatest { (tavern, selected) ->
        if (tavern == null) flowOf(RankingUiState(selected)) else ranking.observeTavern(tavern.id, startOf(selected)).map { RankingUiState(selected, it, tavern.name) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RankingUiState())
    fun select(value: RankingPeriod) { period.value = value }
    private fun startOf(value: RankingPeriod): Long = when (value) {
        RankingPeriod.WEEK -> Instant.now().minus(7, java.time.temporal.ChronoUnit.DAYS).toEpochMilli()
        RankingPeriod.MONTH -> Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS).toEpochMilli()
        RankingPeriod.ALL -> 0L
    }
}
