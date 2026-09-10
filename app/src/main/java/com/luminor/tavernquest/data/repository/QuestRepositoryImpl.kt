package com.luminor.tavernquest.data.repository
import androidx.room.withTransaction
import com.luminor.tavernquest.core.time.DateProvider
import com.luminor.tavernquest.core.time.SystemDateProvider
import com.luminor.tavernquest.core.util.UuidProvider
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import com.luminor.tavernquest.data.local.database.entity.*
import com.luminor.tavernquest.data.remote.firebase.FirebaseMissionRemoteRepository
import com.luminor.tavernquest.domain.model.ContractStatus
import com.luminor.tavernquest.domain.model.SyncStatus
import com.luminor.tavernquest.domain.repository.QuestRepository
import com.luminor.tavernquest.domain.rules.StreakCalculator
import java.time.Instant
import java.time.ZoneId

class QuestRepositoryImpl(
    private val db: TavernQuestDatabase,
    private val ids: UuidProvider,
    private val time: DateProvider = SystemDateProvider(),
    private val remote: FirebaseMissionRemoteRepository? = null,
) : QuestRepository {
    private val streaks = StreakCalculator()
    override suspend fun start(contractId: String, heroId: String): Boolean {
        val mission = db.dailyContractDao().getById(contractId)?.contract ?: return false
        if (mission.heroId != heroId || mission.status != ContractStatus.ACCEPTED.name) return false
        if (mission.startedAt != null) return true
        val remoteStarted = remote?.let { runCatching { it.update(contractId, "START") }.getOrNull() }
        if (remote != null && remoteStarted?.status != ContractStatus.ACCEPTED) return false
        val startedAt = remoteStarted?.startedAt ?: time.nowMillis()
        return db.withTransaction { db.dailyContractDao().start(contractId, startedAt) == 1 }
    }

    override suspend fun complete(contractId: String, heroId: String, notes: String, photoPath: String?): Boolean = db.withTransaction {
        val c = db.dailyContractDao().getById(contractId) ?: return@withTransaction false
        if (c.contract.heroId != heroId || c.contract.status != ContractStatus.ACCEPTED.name) return@withTransaction false
        if (db.heroDao().getHero()?.id != heroId) return@withTransaction false
        if (db.questCompletionDao().countOpenForContract(contractId) > 0) return@withTransaction false
        val startedAt = c.contract.startedAt ?: return@withTransaction false
        val now = time.nowMillis()
        if (now < startedAt || notes.length > 2000) return@withTransaction false
        val date = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate().toString()
        // XP is assigned only after validateCheckIn commits on the server.  The
        // local row is just a pending request and must not change the profile
        // total while the device is offline or the request can still be denied.
        val checkIn = CheckInEntity(
            ids.newId(), contractId, c.template.id, heroId, notes.trim(), photoPath, now,
            c.template.title, c.template.category, 0, startedAt,
            (now - startedAt) / 1000, date, SyncStatus.PENDING_SYNC.name,
        )
        db.questCompletionDao().insert(checkIn)
        val memberships = db.tavernMemberDao().getForHero(heroId)
        db.tavernFeedDao().insertAll(memberships.map { member -> TavernFeedEntity(member.tavernId, checkIn.id, now) })
        // Keep the local mission accepted until validateCheckIn succeeds. This
        // makes the Firebase result, rather than an optimistic device write,
        // authoritative for completion.
        db.activityDao().clearDay(heroId, date)
        db.activityDao().refreshDay(heroId, date)
        refreshStats(heroId)
        db.activityDao().enqueue(PendingSyncEntity(checkIn.id, now))
        true
    }

    private suspend fun refreshStats(heroId: String) {
        db.activityDao().refreshStats(heroId)
        val dates = db.activityDao().activityDates(heroId).mapNotNull { runCatching { java.time.LocalDate.parse(it) }.getOrNull() }.toSet()
        db.activityDao().updateStreaks(heroId, streaks.calculate(dates, time.today()), streaks.longest(dates))
    }
}
