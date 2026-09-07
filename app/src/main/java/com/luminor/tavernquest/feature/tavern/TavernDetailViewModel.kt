package com.luminor.tavernquest.feature.tavern

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.domain.model.CheckIn
import com.luminor.tavernquest.domain.model.Tavern
import com.luminor.tavernquest.domain.repository.TavernFeedRepository
import com.luminor.tavernquest.domain.repository.TavernRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TavernDetailUiState(val tavern: Tavern? = null, val memberCount: Int = 0, val feed: List<CheckIn> = emptyList())

@HiltViewModel
class TavernDetailViewModel @Inject constructor(
    private val taverns: TavernRepository,
    private val feed: TavernFeedRepository,
) : ViewModel() {
    private val tavernId = MutableStateFlow<String?>(null)
    val ui: StateFlow<TavernDetailUiState> = tavernId.flatMapLatest { id ->
        if (id == null) flowOf(TavernDetailUiState())
        else combine(flow { emit(taverns.getById(id)) }, flow { emit(taverns.memberCount(id)) }, feed.observe(id)) { tavern, memberCount, entries -> TavernDetailUiState(tavern, memberCount, entries) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TavernDetailUiState())

    fun load(id: String) { tavernId.value = id }
}
