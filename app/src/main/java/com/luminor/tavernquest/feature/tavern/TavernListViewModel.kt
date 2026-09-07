package com.luminor.tavernquest.feature.tavern

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.domain.model.Tavern
import com.luminor.tavernquest.domain.repository.HeroRepository
import com.luminor.tavernquest.domain.repository.TavernRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TavernListViewModel @Inject constructor(heroes: HeroRepository, tavernRepository: TavernRepository) : ViewModel() {
    val taverns: StateFlow<List<Tavern>> = heroes.observe().flatMapLatest { hero -> if (hero == null) flowOf(emptyList()) else tavernRepository.observeForHero(hero.id) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
