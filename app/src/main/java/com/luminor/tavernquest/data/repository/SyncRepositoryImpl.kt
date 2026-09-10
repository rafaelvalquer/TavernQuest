package com.luminor.tavernquest.data.repository

import androidx.room.withTransaction
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import com.luminor.tavernquest.domain.model.SyncStatus
import com.luminor.tavernquest.domain.model.ContractStatus
import com.luminor.tavernquest.domain.repository.SyncRepository
import com.luminor.tavernquest.domain.rules.StreakCalculator
import com.luminor.tavernquest.core.time.DateProvider
import com.luminor.tavernquest.core.time.SystemDateProvider

class SyncRepositoryImpl(private val db: TavernQuestDatabase, private val time: DateProvider = SystemDateProvider()) : SyncRepository {
    private val streaks = StreakCalculator()
    private val dao get() = db.activityDao()
    override suspend fun pendingCount() = dao.countPending()
    override suspend fun markValidated(checkInId: String, officialXp: Int) = db.withTransaction {
        val checkIn = dao.getCheckIn(checkInId) ?: return@withTransaction
        val correctedXp = officialXp.coerceAtLeast(0)
        dao.updateValidated(checkInId, SyncStatus.VALIDATED.name, correctedXp)
        db.dailyContractDao().getById(checkIn.dailyContractId)?.contract?.let { contract ->
            db.dailyContractDao().updateStatus(checkIn.dailyContractId, ContractStatus.COMPLETED.name, contract.acceptedAt, checkIn.completedAt)
        }
        dao.clearDay(checkIn.heroId, checkIn.activityDate)
        dao.refreshDay(checkIn.heroId, checkIn.activityDate)
        refreshStats(checkIn.heroId)
        dao.removePending(checkInId)
    }
    override suspend fun markRejected(checkInId: String, error: String?) = db.withTransaction {
        val checkIn = dao.getCheckIn(checkInId) ?: return@withTransaction
        dao.updateSyncStatus(checkInId, SyncStatus.REJECTED.name)
        dao.clearDay(checkIn.heroId, checkIn.activityDate)
        dao.refreshDay(checkIn.heroId, checkIn.activityDate)
        refreshStats(checkIn.heroId)
        dao.removePending(checkInId)
    }
    override suspend fun markFailed(checkInId: String, error: String?) = dao.recordSyncFailure(checkInId, error?.take(500))

    private suspend fun refreshStats(heroId: String) {
        dao.refreshStats(heroId)
        val dates = dao.activityDates(heroId).mapNotNull { runCatching { java.time.LocalDate.parse(it) }.getOrNull() }.toSet()
        dao.updateStreaks(heroId, streaks.calculate(dates, time.today()), streaks.longest(dates))
    }
}
