package com.luminor.tavernquest.data.repository

import androidx.room.withTransaction
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import com.luminor.tavernquest.domain.model.SyncStatus
import com.luminor.tavernquest.domain.repository.SyncRepository

class SyncRepositoryImpl(private val db: TavernQuestDatabase) : SyncRepository {
    private val dao get() = db.activityDao()
    private val heroes get() = db.heroDao()
    override suspend fun pendingCount() = dao.countPending()
    override suspend fun markValidated(checkInId: String, officialXp: Int) = db.withTransaction {
        val checkIn = dao.getCheckIn(checkInId) ?: return@withTransaction
        val correctedXp = officialXp.coerceAtLeast(0)
        dao.updateValidated(checkInId, SyncStatus.VALIDATED.name, correctedXp)
        val delta = correctedXp - checkIn.xpEarned
        if (delta != 0) heroes.updateXp(checkIn.heroId, delta)
        dao.refreshDay(checkIn.heroId, checkIn.activityDate)
        dao.refreshStats(checkIn.heroId)
        dao.removePending(checkInId)
    }
    override suspend fun markRejected(checkInId: String, error: String?) = db.withTransaction {
        val checkIn = dao.getCheckIn(checkInId) ?: return@withTransaction
        dao.updateSyncStatus(checkInId, SyncStatus.REJECTED.name)
        if (checkIn.xpEarned != 0) heroes.updateXp(checkIn.heroId, -checkIn.xpEarned)
        dao.refreshDay(checkIn.heroId, checkIn.activityDate)
        dao.refreshStats(checkIn.heroId)
        dao.removePending(checkInId)
    }
    override suspend fun markFailed(checkInId: String, error: String?) = dao.recordSyncFailure(checkInId, error?.take(500))
}
