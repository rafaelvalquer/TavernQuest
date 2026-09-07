package com.luminor.tavernquest.usecase

import com.luminor.tavernquest.core.time.DateProvider
import com.luminor.tavernquest.core.util.UuidProvider
import com.luminor.tavernquest.domain.model.GameSettings
import com.luminor.tavernquest.domain.model.Hero
import com.luminor.tavernquest.domain.model.HeroAppearance
import com.luminor.tavernquest.domain.model.HeroClass
import com.luminor.tavernquest.domain.model.Tavern
import com.luminor.tavernquest.domain.model.TavernEmblem
import com.luminor.tavernquest.domain.model.TavernMember
import com.luminor.tavernquest.domain.repository.HeroRepository
import com.luminor.tavernquest.domain.repository.SettingsRepository
import com.luminor.tavernquest.domain.repository.TavernRepository
import com.luminor.tavernquest.domain.usecase.onboarding.CreateTavernUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateTavernUseCaseTest {
    @Test fun createsPrivateTavernWithOwnerAndCompletesOnboarding() = runBlocking {
        val hero = Hero("hero", "Rafael", HeroAppearance.MASCULINE, HeroClass.WARRIOR, 0, 0)
        val taverns = CapturingTaverns()
        val settings = CapturingSettings()
        val ids = object : UuidProvider { private var next = 0; override fun newId() = listOf("tavern-id", "member-id")[next++] }
        val time = object : DateProvider { override fun nowMillis() = 1234L; override fun today() = java.time.LocalDate.of(2026, 9, 7) }

        CreateTavernUseCase(taverns, object : HeroRepository {
            override suspend fun create(hero: Hero) = Unit
            override suspend fun get() = hero
            override fun observe(): Flow<Hero?> = emptyFlow()
            override suspend fun addXp(heroId: String, amount: Int) = Unit
        }, settings, ids, time)(" Alcateia ", TavernEmblem.WOLF, "Aventura matinal", true)

        assertEquals("tavern-id", taverns.tavern?.id)
        assertEquals("Alcateia", taverns.tavern?.name)
        assertEquals("Aventura matinal", taverns.tavern?.description)
        assertTrue(taverns.tavern?.isPrivate == true)
        assertEquals("hero", taverns.owner?.heroId)
        assertTrue(settings.completed)
    }

    private class CapturingSettings : SettingsRepository {
        var completed = false
        override fun observe(): Flow<GameSettings> = emptyFlow()
        override suspend fun setOnboardingCompleted(v: Boolean) { completed = v }
        override suspend fun setSound(v: Boolean) = Unit
        override suspend fun setHaptics(v: Boolean) = Unit
        override suspend fun reset() = Unit
    }

    private class CapturingTaverns : TavernRepository {
        var tavern: Tavern? = null
        var owner: TavernMember? = null
        override suspend fun create(tavern: Tavern, owner: TavernMember) { this.tavern = tavern; this.owner = owner }
        override suspend fun get(): Tavern? = null
        override suspend fun getById(id: String): Tavern? = null
        override suspend fun memberCount(tavernId: String) = 0
        override suspend fun members(tavernId: String) = emptyList<TavernMember>()
        override fun observe(): Flow<Tavern?> = emptyFlow()
        override fun observeForHero(heroId: String): Flow<List<Tavern>> = emptyFlow()
        override suspend fun getByCode(code: String): Tavern? = null
        override suspend fun join(tavernId: String, member: TavernMember) = false
        override suspend fun leave(tavernId: String, heroId: String) = Unit
    }
}
