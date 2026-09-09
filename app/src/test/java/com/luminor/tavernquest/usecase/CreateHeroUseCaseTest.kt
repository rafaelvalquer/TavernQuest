package com.luminor.tavernquest.usecase

import com.luminor.tavernquest.core.time.DateProvider
import com.luminor.tavernquest.core.util.UuidProvider
import com.luminor.tavernquest.domain.model.AuthUser
import com.luminor.tavernquest.domain.model.Hero
import com.luminor.tavernquest.domain.model.HeroAppearance
import com.luminor.tavernquest.domain.model.HeroClass
import com.luminor.tavernquest.domain.repository.AuthRepository
import com.luminor.tavernquest.domain.repository.HeroRepository
import com.luminor.tavernquest.domain.usecase.onboarding.CreateHeroUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CreateHeroUseCaseTest {
    @Test fun createsTrimmedHeroForAuthenticatedUser() = runBlocking {
        val repo = CapturingHeroRepository()
        val auth = object : AuthRepository {
            override val currentUser = flowOf(AuthUser("firebase-user", "r@example.com", "Rafael"))
            override suspend fun loginWithGoogleIdToken(idToken: String) = error("unused")
            override fun logout() = Unit
        }
        CreateHeroUseCase(repo, object : UuidProvider { override fun newId() = "hero-id" }, fixedTime, auth)("  Rafael  ", HeroClass.MAGE, HeroAppearance.FEMININE)

        assertEquals(Hero("firebase-user", "Rafael", HeroAppearance.FEMININE, HeroClass.MAGE, 0, 1234L, "firebase-user"), repo.created)
    }

    private class CapturingHeroRepository : HeroRepository {
        var created: Hero? = null
        override suspend fun create(hero: Hero) { created = hero }
        override suspend fun get(): Hero? = null
        override fun observe(): Flow<Hero?> = emptyFlow()
        override suspend fun addXp(heroId: String, amount: Int) = Unit
    }

    private companion object {
        val fixedTime = object : DateProvider {
            override fun nowMillis() = 1234L
            override fun today() = LocalDate.of(2026, 9, 8)
        }
    }
}
