package com.luminor.tavernquest.feature.onboarding.tavern

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.domain.model.TavernEmblem
import com.luminor.tavernquest.domain.usecase.onboarding.CreateTavernUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TavernCreationViewModel @Inject constructor(
    private val create: CreateTavernUseCase,
) : ViewModel() {
    private val _ui = MutableStateFlow(TavernCreationUiState())
    val ui = _ui.asStateFlow()

    fun name(value: String) = _ui.update { it.copy(name = value, error = null) }
    fun description(value: String) = _ui.update { it.copy(description = value) }
    fun setPrivate(value: Boolean) = _ui.update { it.copy(isPrivate = value) }
    fun emblem(value: TavernEmblem) = _ui.update { it.copy(emblem = value) }

    fun save() {
        val state = _ui.value
        if (state.name.isBlank()) return
        viewModelScope.launch {
            _ui.update { it.copy(saving = true, error = null) }
            runCatching { create(state.name, state.emblem, state.description, state.isPrivate) }
                .onSuccess { _ui.update { it.copy(saving = false, created = true) } }
                .onFailure { error -> _ui.update { it.copy(saving = false, error = friendlyError(error)) } }
        }
    }

    private fun friendlyError(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("unauthenticated", ignoreCase = true) -> "Entre novamente com Google para fundar a Taberna."
            message.contains("failed-precondition", ignoreCase = true) || message.contains("Crie seu herói", ignoreCase = true) -> "Conclua a criação do herói antes de fundar a Taberna."
            message.contains("already-exists", ignoreCase = true) -> "O convite entrou em conflito. Toque em fundar novamente."
            message.contains("Field '", ignoreCase = true) || message.contains("INTERNAL", ignoreCase = true) -> "Não foi possível concluir agora. Tente novamente em alguns segundos."
            else -> message.ifBlank { "Não foi possível sincronizar a Taberna. Verifique sua conexão e tente novamente." }
        }
    }
}