package com.luminor.tavernquest.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(val loading: Boolean = false, val error: String? = null, val authenticated: Boolean = false, val needsHero: Boolean = false)

@HiltViewModel
class AuthViewModel @Inject constructor(private val auth: AuthRepository) : ViewModel() {
    private val _ui = MutableStateFlow(AuthUiState())
    val ui = _ui.asStateFlow()
    fun googleToken(idToken: String) = viewModelScope.launch {
        _ui.update { it.copy(loading = true, error = null) }
        val result = auth.loginWithGoogleIdToken(idToken)
        result.fold(
            onSuccess = { _ui.update { state -> state.copy(loading = false, authenticated = true) } },
            onFailure = { error -> showFailure(error, LoginFailureStage.FIREBASE_AUTH) },
        )
    }
    fun googleUnavailable(message: String = "Configure o google-services.json para entrar com Google.") { _ui.update { it.copy(loading = false, error = message) } }

    fun googleCredentialFailure(error: Throwable) = showFailure(error, LoginFailureStage.GOOGLE_CREDENTIAL)

    private fun showFailure(error: Throwable, stage: LoginFailureStage) {
        reportAuthFailure(error, stage)
        _ui.update { it.copy(loading = false, error = loginFailureMessage(error, stage)) }
    }
}
