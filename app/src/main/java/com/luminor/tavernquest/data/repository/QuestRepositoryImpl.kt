package com.luminor.tavernquest.data.repository
import androidx.room.withTransaction
import com.luminor.tavernquest.core.time.DateProvider
import com.luminor.tavernquest.core.time.SystemDateProvider
import com.luminor.tavernquest.core.util.UuidProvider
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import com.luminor.tavernquest.data.local.database.entity.*
import com.luminor.tavernquest.domain.model.ContractStatus
import com.luminor.tavernquest.domain.model.SyncStatus
import com.luminor.tavernquest.domain.repository.QuestRepository
import java.time.Instant
import java.time.ZoneId

class QuestRepositoryImpl(
    private val db: TavernQuestDatabase,
    private val ids: UuidProvider,
    private val time: DateProvider = SystemDateProvider(),
) : QuestRepository {
    override suspend fun start(contractId: String, heroId: String): Boolean = db.withTransaction {
        val mission = db.dailyContractDao().getById(contractId)?.contract ?: return@withTransaction false
        if (mission.heroId != heroId || mission.status != ContractStatus.ACCEPTED.name) return@withTransaction false
        if (mission.startedAt != null) return@withTransaction true
        db.dailyContractDao().start(contractId, time.nowMillis()) == 1
    }

    override suspend fun complete(contractId: String, heroId: String, notes: String, photoPath: String?): Boolean = db.withTransaction {
        val c = db.dailyContractDao().getById(contractId) ?: return@withTransaction false
        if (c.contract.heroId != heroId || c.contract.status != ContractStatus.ACCEPTED.name) return@withTransaction false
        if (db.heroDao().getHero()?.id != heroId) return@withTransaction false
        val startedAt = c.contract.startedAt ?: return@withTransaction false
        val now = time.nowMillis()
        if (now < startedAt || notes.length > 2000) return@withTransaction false
        val date = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate().toString()
        val ledger = XpLedgerEntity(ids.newId(), heroId, c.template.xpReward, "QUEST", contractId, now)
        if (db.xpLedgerDao().insert(ledger) == -1L) return@withTransaction false
        val checkIn = CheckInEntity(
            ids.newId(), contractId, heroId, notes.trim(), photoPath, now,
            c.template.title, c.template.category, c.template.xpReward, startedAt,
            (now - startedAt) / 1000, date, SyncStatus.PENDING_SYNC.name,
        )
        db.questCompletionDao().insert(checkIn)
        db.heroDao().updateXp(heroId, c.template.xpReward)
        db.dailyContractDao().updateStatus(contractId, ContractStatus.COMPLETED.name, c.contract.acceptedAt, now)
        db.activityDao().refreshDay(heroId, date)
        db.activityDao().refreshStats(heroId)
        db.activityDao().enqueue(PendingSyncEntity(checkIn.id, now))
        true
    }
}
