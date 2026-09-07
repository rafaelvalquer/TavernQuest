package com.luminor.tavernquest.feature.tavern

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luminor.tavernquest.core.time.DateProvider
import com.luminor.tavernquest.core.util.UuidProvider
import com.luminor.tavernquest.domain.model.TavernMember
import com.luminor.tavernquest.domain.model.TavernRole
import com.luminor.tavernquest.domain.repository.HeroRepository
import com.luminor.tavernquest.domain.repository.TavernRepository
import com.luminor.tavernquest.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JoinTavernUiState(val code: String = "", val saving: Boolean = false, val joined: Boolean = false, val error: String? = null)

@HiltViewModel
class JoinTavernViewModel @Inject constructor(private val taverns: TavernRepository, private val heroes: HeroRepository, private val settings: SettingsRepository, private val ids: UuidProvider, private val time: DateProvider) : ViewModel() {
    private val _ui = MutableStateFlow(JoinTavernUiState())
    val ui = _ui.asStateFlow()
    fun code(value: String) { _ui.update { it.copy(code = value.filter(Char::isLetterOrDigit).take(6).uppercase(), error = null) } }
    fun join() = viewModelScope.launch {
        val code = _ui.value.code
        if (code.length != 6) { _ui.update { it.copy(error = "Digite o código de 6 caracteres.") }; return@launch }
        val hero = heroes.get() ?: run { _ui.update { it.copy(error = "Crie seu herói antes de entrar.") }; return@launch }
        _ui.update { it.copy(saving = true) }
        val tavern = taverns.getByCode(code) ?: run { _ui.update { it.copy(saving = false, error = "Código não encontrado.") }; return@launch }
        val ok = taverns.join(tavern.id, TavernMember(ids.newId(), tavern.id, hero.id, TavernRole.MEMBER, time.nowMillis()))
        if (ok) settings.setOnboardingCompleted(true)
        _ui.update { it.copy(saving = false, joined = ok, error = if (ok) null else "Não foi possível entrar na Taberna.") }
    }
}
