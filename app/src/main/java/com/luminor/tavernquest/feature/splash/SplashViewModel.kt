package com.luminor.tavernquest.feature.splash

import com.luminor.tavernquest.feature.auth.LoginFailureStage
import com.luminor.tavernquest.feature.auth.reportAuthFailure
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.domain.repository.AuthRepository
import com.luminor.tavernquest.domain.repository.HeroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val heroes: HeroRepository,
) : ViewModel() {
    private val refresh = MutableStateFlow(0)

    val state = combine(auth.currentUser, refresh) { user, _ -> user }
        .flatMapLatest { user ->
            if (user == null) flowOf(sessionStateAfterAccount(userPresent = false, heroPresent = false))
            else flow {
                emit(SessionBootstrapState.Loading)
                val account = auth.ensureAccount()
                if (account.isFailure) {
                    emitFailure(account.exceptionOrNull() ?: IllegalStateException("Não foi possível preparar a conta."))
                } else {
                    val profile = runCatching { heroes.get() }
                    if (profile.isFailure) emitFailure(profile.exceptionOrNull() ?: IllegalStateException("Não foi possível carregar o perfil."))
                    else emit(sessionStateAfterAccount(userPresent = true, heroPresent = profile.getOrNull() != null))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionBootstrapState.Loading)

    fun retry() { refresh.value += 1 }
    fun logout() { auth.logout() }

    private suspend fun FlowCollector<SessionBootstrapState>.emitFailure(error: Throwable) {
        reportAuthFailure(error, LoginFailureStage.ACCOUNT_SETUP)
        emit(SessionBootstrapState.Error(sessionBootstrapFailure(error)))
    }
}
