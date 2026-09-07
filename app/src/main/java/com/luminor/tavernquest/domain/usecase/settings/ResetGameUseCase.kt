package com.luminor.tavernquest.domain.usecase.settings
import javax.inject.Inject
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase;import com.luminor.tavernquest.domain.repository.SettingsRepository
class ResetGameUseCase @Inject constructor(private val db:TavernQuestDatabase,private val settings:SettingsRepository){suspend operator fun invoke(){db.clearAllTables();settings.reset()}}
