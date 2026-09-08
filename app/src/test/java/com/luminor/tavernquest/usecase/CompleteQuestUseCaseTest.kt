package com.luminor.tavernquest.usecase

import com.luminor.tavernquest.domain.model.Hero
import com.luminor.tavernquest.domain.model.HeroAppearance
import com.luminor.tavernquest.domain.model.HeroClass
import com.luminor.tavernquest.domain.repository.HeroRepository
import com.luminor.tavernquest.domain.repository.QuestRepository
import com.luminor.tavernquest.domain.usecase.quest.CompleteQuestUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteQuestUseCaseTest {
    @Test fun completesForCurrentHeroAndPreservesProofData() = runBlocking {
        val quests = CapturingQuests()
        val hero = Hero("hero-1", "Rafael", HeroAppearance.MASCULINE, HeroClass.WARRIOR, 0, 0)
        val result = CompleteQuestUseCase(quests, FixedHeroRepository(hero))("contract-1", "  treino feito  ", "photo.jpg")

        assertTrue(result)
        assertEquals(listOf("contract-1", "hero-1", "  treino feito  ", "photo.jpg"), quests.arguments)
    }

    @Test fun doesNotCompleteWhenThereIsNoHero() = runBlocking {
        val quests = CapturingQuests()
        assertFalse(CompleteQuestUseCase(quests, FixedHeroRepository(null))("contract-1", "notes", null))
        assertEquals(null, quests.arguments)
    }

    private class FixedHeroRepository(private val hero: Hero?) : HeroRepository {
        override suspend fun create(hero: Hero) = Unit
        override suspend fun get(): Hero? = hero
        override fun observe(): Flow<Hero?> = emptyFlow()
        override suspend fun addXp(heroId: String, amount: Int) = Unit
    }

    private class CapturingQuests : QuestRepository {
        var arguments: List<String?>? = null
        override suspend fun start(contractId: String, heroId: String) = false
        override suspend fun complete(contractId: String, heroId: String, notes: String, photoPath: String?): Boolean {
            arguments = listOf(contractId, heroId, notes, photoPath)
            return true
        }
    }
}
