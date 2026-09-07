package com.luminor.tavernquest.domain.repository
import com.luminor.tavernquest.domain.model.QuestCompletion; import kotlinx.coroutines.flow.Flow
interface JournalRepository{fun observeAll():Flow<List<QuestCompletion>>;suspend fun getCompletedDates():List<String>}
