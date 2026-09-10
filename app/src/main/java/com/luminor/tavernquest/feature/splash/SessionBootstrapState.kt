package com.luminor.tavernquest.feature.splash

import com.luminor.tavernquest.feature.auth.LoginFailureStage
import com.luminor.tavernquest.feature.auth.loginFailureMessage

sealed interface SessionBootstrapState {
    data object Loading : SessionBootstrapState
    data object NeedsLogin : SessionBootstrapState
    data object NeedsHero : SessionBootstrapState
    data object Ready : SessionBootstrapState
    data class Error(val message: String) : SessionBootstrapState
}

internal fun sessionBootstrapFailure(error: Throwable) =
    loginFailureMessage(error, LoginFailureStage.ACCOUNT_SETUP)

internal fun sessionStateAfterAccount(userPresent: Boolean, heroPresent: Boolean): SessionBootstrapState =
    when {
        !userPresent -> SessionBootstrapState.NeedsLogin
        !heroPresent -> SessionBootstrapState.NeedsHero
        else -> SessionBootstrapState.Ready
    }
