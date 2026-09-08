package com.luminor.tavernquest.feature.tavern

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.domain.model.CheckIn
import com.luminor.tavernquest.domain.model.Tavern
import com.luminor.tavernquest.domain.model.TavernMember
import com.luminor.tavernquest.domain.model.RankingEntry
import com.luminor.tavernquest.domain.model.TavernRole
import com.luminor.tavernquest.domain.repository.TavernFeedRepository
import com.luminor.tavernquest.domain.repository.TavernRepository
import com.luminor.tavernquest.domain.repository.RankingRepository
import com.luminor.tavernquest.domain.repository.HeroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TavernDetailTab { FEED, RANKING, MEMBERS }
enum class TavernRankingPeriod { WEEK, MONTH, ALL }
data class TavernDetailUiState(val tavern: Tavern? = null, val memberCount: Int = 0, val feed: List<CheckIn> = emptyList(), val members: List<TavernMember> = emptyList(), val ranking: List<RankingEntry> = emptyList(), val tab: TavernDetailTab = TavernDetailTab.FEED, val period: TavernRankingPeriod = TavernRankingPeriod.WEEK, val leaving: Boolean = false, val left: Boolean = false)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class TavernDetailViewModel @Inject constructor(
    private val taverns: TavernRepository,
    private val feed: TavernFeedRepository,
    private val rankings: RankingRepository,
    private val heroes: HeroRepository,
) : ViewModel() {
    private val tavernId = MutableStateFlow<String?>(null)
    private val tab = MutableStateFlow(TavernDetailTab.FEED)
    private val period = MutableStateFlow(TavernRankingPeriod.WEEK)
    private val left = MutableStateFlow(false)
    private val content = combine(tavernId, tab, period) { id, selectedTab, selectedPeriod -> Triple(id, selectedTab, selectedPeriod) }.flatMapLatest { (id, selectedTab, selectedPeriod) ->
        if (id == null) flowOf(TavernDetailUiState())
        else combine(flow { emit(taverns.getById(id)) }, taverns.observeMembers(id), feed.observe(id), rankings.observeTavern(id, periodStart(selectedPeriod))) { tavern, members, entries, ranking -> TavernDetailUiState(tavern, members.size, entries, members, ranking, selectedTab, selectedPeriod) }
    }
    val ui: StateFlow<TavernDetailUiState> = combine(content, left) { state, hasLeft -> state.copy(left = hasLeft) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TavernDetailUiState())

    fun load(id: String) { tavernId.value = id }
    fun selectTab(value: TavernDetailTab) { tab.value = value }
    fun selectPeriod(value: TavernRankingPeriod) { period.value = value }
    fun leave() = viewModelScope.launch {
        val id = tavernId.value ?: return@launch
        val hero = heroes.get() ?: return@launch
        taverns.leave(id, hero.id)
        left.value = true
    }
    private fun periodStart(value: TavernRankingPeriod): Long = when (value) {
        TavernRankingPeriod.WEEK -> java.time.Instant.now().minus(7, java.time.temporal.ChronoUnit.DAYS).toEpochMilli()
        TavernRankingPeriod.MONTH -> java.time.Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS).toEpochMilli()
        TavernRankingPeriod.ALL -> 0L
    }
}
