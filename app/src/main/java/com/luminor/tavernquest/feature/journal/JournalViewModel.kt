package com.luminor.tavernquest.feature.journal
import androidx.lifecycle.*;import com.luminor.tavernquest.domain.usecase.journal.GetJournalUseCase;import dagger.hilt.android.lifecycle.HiltViewModel;import javax.inject.Inject;import kotlinx.coroutines.flow.*
@HiltViewModel class JournalViewModel @Inject constructor(get:GetJournalUseCase):ViewModel(){val ui=get().map{JournalUiState(it)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),JournalUiState())}
