package com.luminor.tavernquest.feature.quest.detail
import androidx.lifecycle.*
import com.luminor.tavernquest.domain.usecase.contract.AcceptContractUseCase
import com.luminor.tavernquest.domain.usecase.mission.StartMissionUseCase
import com.luminor.tavernquest.domain.usecase.quest.GetQuestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class QuestDetailViewModel @Inject constructor(
    saved: SavedStateHandle,
    private val get: GetQuestUseCase,
    private val acceptUse: AcceptContractUseCase,
    private val startUse: StartMissionUseCase,
) : ViewModel() {
    private val id: String = saved["id"] ?: ""
    private val _ui = MutableStateFlow(QuestDetailUiState())
    val ui = _ui.asStateFlow()
    init { viewModelScope.launch { _ui.value = QuestDetailUiState(get(id), false) } }
    fun accept() = act { acceptUse(id) }
    fun start() = act { startUse(id) }
    private fun act(action: suspend () -> Boolean) {
        viewModelScope.launch {
            try {
                val ok = action()
                _ui.update { it.copy(quest = get(id), message = if (ok) null else "Não foi possível atualizar esta missão.") }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) { _ui.update { it.copy(message = "Não foi possível salvar. Tente novamente.") } }
        }
    }
}
