package com.luminor.tavernquest.domain.usecase.mission

import com.luminor.tavernquest.domain.repository.HeroRepository
import com.luminor.tavernquest.domain.repository.QuestRepository
import javax.inject.Inject

class StartMissionUseCase @Inject constructor(
    private val quests: QuestRepository,
    private val heroes: HeroRepository,
) {
    suspend operator fun invoke(id: String): Boolean {
        val hero = heroes.get() ?: return false
        return quests.start(id, hero.id)
    }
}
