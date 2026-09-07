package com.luminor.tavernquest.feature.journal
import com.luminor.tavernquest.domain.model.QuestCompletion
data class JournalUiState(val entries:List<QuestCompletion> = emptyList())
