package com.luminor.tavernquest.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.domain.repository.AuthRepository
import com.luminor.tavernquest.domain.repository.HeroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(val loading: Boolean = false, val error: String? = null, val authenticated: Boolean = false, val needsHero: Boolean = false)

@HiltViewModel
class AuthViewModel @Inject constructor(private val auth: AuthRepository, private val heroes: HeroRepository) : ViewModel() {
    private val _ui = MutableStateFlow(AuthUiState())
    val ui = _ui.asStateFlow()
    fun googleToken(idToken: String) = viewModelScope.launch {
        _ui.update { it.copy(loading = true, error = null) }
        val result = auth.loginWithGoogleIdToken(idToken)
        result.fold(onSuccess = {
            auth.ensureAccount().fold(
                onSuccess = {
                    val needsHero = heroes.get() == null
                    _ui.update { state -> state.copy(loading = false, authenticated = true, needsHero = needsHero) }
                },
                onFailure = { error -> _ui.update { state -> state.copy(loading = false, error = friendlyError(error)) } },
            )
        }, onFailure = { error -> _ui.update { current -> current.copy(loading = false, error = friendlyError(error)) } })
    }
    fun googleUnavailable(message: String = "Configure o google-services.json para entrar com Google.") { _ui.update { it.copy(loading = false, error = message) } }

    private fun friendlyError(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("network", ignoreCase = true) || message.contains("unavailable", ignoreCase = true) -> "Não foi possível conectar agora. Verifique sua internet e tente novamente."
            message.contains("cancel", ignoreCase = true) -> "A entrada com Google foi cancelada."
            message.contains("Firebase não configurado", ignoreCase = true) -> "Este aplicativo ainda não está configurado para entrar com Google."
            else -> "Não foi possível entrar com Google. Tente novamente."
        }
    }
}
