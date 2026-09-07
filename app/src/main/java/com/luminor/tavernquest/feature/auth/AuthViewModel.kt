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

data class AuthUiState(val email: String = "", val password: String = "", val displayName: String = "", val loading: Boolean = false, val error: String? = null, val authenticated: Boolean = false, val needsHero: Boolean = false)

@HiltViewModel
class AuthViewModel @Inject constructor(private val auth: AuthRepository, private val heroes: HeroRepository) : ViewModel() {
    private val _ui = MutableStateFlow(AuthUiState())
    val ui = _ui.asStateFlow()
    fun email(value: String) = _ui.update { it.copy(email = value, error = null) }
    fun password(value: String) = _ui.update { it.copy(password = value, error = null) }
    fun displayName(value: String) = _ui.update { it.copy(displayName = value, error = null) }
    fun googleToken(idToken: String) = viewModelScope.launch {
        _ui.update { it.copy(loading = true, error = null) }
        val result = auth.loginWithGoogleIdToken(idToken)
        val needsHero = result.isSuccess && heroes.get() == null
        result.fold(onSuccess = { _ui.update { it.copy(loading = false, authenticated = true, needsHero = needsHero) } }, onFailure = { error -> _ui.update { current -> current.copy(loading = false, error = error.message ?: "Não foi possível entrar com Google.") } })
    }
    fun googleUnavailable() { _ui.update { it.copy(error = "Configure o google-services.json para entrar com Google.") } }
    fun submit(register: Boolean) = viewModelScope.launch {
        val state = _ui.value
        if (!state.email.contains('@') || state.password.length < 6 || (register && state.displayName.isBlank())) {
            _ui.update { it.copy(error = if (register) "Informe nome, e-mail válido e senha com 6 caracteres." else "Informe e-mail válido e senha com 6 caracteres.") }
            return@launch
        }
        _ui.update { it.copy(loading = true, error = null) }
        val result = if (register) auth.register(state.email, state.password, state.displayName) else auth.login(state.email, state.password)
        val needsHero = result.isSuccess && heroes.get() == null
        result.fold(onSuccess = { _ui.update { it.copy(loading = false, authenticated = true, needsHero = needsHero) } }, onFailure = { error -> _ui.update { current -> current.copy(loading = false, error = error.message ?: "Não foi possível autenticar.") } })
    }
}
