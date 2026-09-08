package com.luminor.tavernquest.usecase

import com.luminor.tavernquest.domain.model.DailyContract
import com.luminor.tavernquest.domain.repository.ContractRepository
import com.luminor.tavernquest.domain.usecase.contract.AbandonContractUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AbandonContractUseCaseTest {
    @Test fun abandonsTheRequestedContract() = runBlocking {
        val repo = CapturingContracts()
        AbandonContractUseCase(repo)("contract-9")
        assertEquals("contract-9", repo.abandoned)
    }

    private class CapturingContracts : ContractRepository {
        var abandoned: String? = null
        override suspend fun ensureSeeded() = Unit
        override suspend fun generateBoard(date: String, heroId: String) = Unit
        override suspend fun getBoard(date: String): List<DailyContract> = emptyList()
        override fun observeBoard(date: String): Flow<List<DailyContract>> = emptyFlow()
        override suspend fun get(id: String): DailyContract? = null
        override suspend fun accept(id: String, date: String) = false
        override suspend fun abandon(id: String) { abandoned = id }
        override suspend fun expireBefore(date: String) = Unit
    }
}
