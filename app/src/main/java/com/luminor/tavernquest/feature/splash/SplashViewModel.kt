package com.luminor.tavernquest.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.domain.repository.AuthRepository
import com.luminor.tavernquest.domain.repository.HeroRepository
import com.luminor.tavernquest.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SplashViewModel @Inject constructor(auth: AuthRepository, heroes: HeroRepository) : ViewModel() {
    val route = auth.currentUser.flatMapLatest { user ->
        if (user == null) flowOf(AppRoute.Welcome)
        else heroes.observe().map { hero -> if (hero == null) AppRoute.HeroCreation else AppRoute.Home }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
