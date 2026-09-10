package com.luminor.tavernquest.domain.usecase.settings
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase;import com.luminor.tavernquest.domain.repository.SettingsRepository;import com.luminor.tavernquest.domain.repository.AuthRepository
class ResetGameUseCase @Inject constructor(private val db:TavernQuestDatabase,private val settings:SettingsRepository,private val auth:AuthRepository){suspend operator fun invoke(){withContext(Dispatchers.IO){db.clearAllTables()};settings.reset();auth.logout()}}

