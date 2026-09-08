package com.luminor.tavernquest.usecase

import com.luminor.tavernquest.core.time.DateProvider
import com.luminor.tavernquest.domain.model.DailyContract
import com.luminor.tavernquest.domain.repository.ContractRepository
import com.luminor.tavernquest.domain.usecase.contract.AcceptContractUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AcceptContractUseCaseTest {
    @Test fun acceptsContractUsingTheProviderDate() = runBlocking {
        val repo = CapturingContracts()
        val accepted = AcceptContractUseCase(repo, object : DateProvider {
            override fun nowMillis() = 0L
            override fun today() = LocalDate.of(2026, 9, 8)
        })("contract-7")

        assertTrue(accepted)
        assertEquals("contract-7" to "2026-09-08", repo.accepted)
    }

    private class CapturingContracts : ContractRepository {
        var accepted: Pair<String, String>? = null
        override suspend fun ensureSeeded() = Unit
        override suspend fun generateBoard(date: String, heroId: String) = Unit
        override suspend fun getBoard(date: String): List<DailyContract> = emptyList()
        override fun observeBoard(date: String): Flow<List<DailyContract>> = emptyFlow()
        override suspend fun get(id: String): DailyContract? = null
        override suspend fun accept(id: String, date: String): Boolean { accepted = id to date; return true }
        override suspend fun abandon(id: String) = Unit
        override suspend fun expireBefore(date: String) = Unit
    }
}
