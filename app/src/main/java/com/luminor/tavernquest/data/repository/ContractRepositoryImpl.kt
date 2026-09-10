package com.luminor.tavernquest.data.repository
import androidx.room.withTransaction
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import com.luminor.tavernquest.data.local.database.entity.DailyContractEntity
import com.luminor.tavernquest.data.mapper.*
import com.luminor.tavernquest.data.seed.ContractSeed
import com.luminor.tavernquest.data.remote.firebase.FirebaseMissionRemoteRepository
import com.luminor.tavernquest.domain.model.*
import com.luminor.tavernquest.domain.repository.ContractRepository
import com.luminor.tavernquest.domain.rules.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class ContractRepositoryImpl(private val db: TavernQuestDatabase, private val remote: FirebaseMissionRemoteRepository? = null) : ContractRepository {
    private val templates = db.contractTemplateDao()
    private val daily = db.dailyContractDao()
    private val gen = DailyBoardGenerator()
    private val limit = ContractLimitRule()
    override suspend fun ensureSeeded() { if (templates.count() == 0) templates.insertAll(ContractSeed.all.map { it.toEntity() }) }
    override suspend fun generateBoard(date: String, heroId: String) {
        // A network failure must keep the last Room projection visible.  It must
        // never fall back to generating a different local board for a Firebase
        // account, because that occurrence would be rejected by the server.
        val remoteBoard = remote?.let { runCatching { it.board(date) }.getOrNull() }
        if (remote != null && remoteBoard == null) return
        db.withTransaction {
            if (remoteBoard != null) {
                val board = remoteBoard
                templates.insertAll(board.map { it.template.toEntity() })
                daily.deleteForDate(heroId, date)
                daily.upsertAll(board.map { item -> DailyContractEntity(item.occurrenceId, item.template.id, heroId, item.date, item.status.name, item.acceptedAt, item.completedAt, item.startedAt) })
                return@withTransaction
            }
            ensureSeeded()
            if (daily.getByDate(date, heroId).any { it.contract.date == date }) return@withTransaction
            val chosen = gen.generate(templates.getEnabled().map { it.toDomain() }, date)
            daily.insertAll(chosen.map { DailyContractEntity("$heroId:$date:${it.id}", it.id, heroId, date, ContractStatus.AVAILABLE.name, null, null) })
        }
    }
    override suspend fun getBoard(date: String): List<DailyContract> {
        val hero = db.heroDao().getHero() ?: return emptyList()
        return daily.getByDate(date, hero.id).map { it.toDomain() }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeBoard(date: String): Flow<List<DailyContract>> = db.heroDao().observeHero().flatMapLatest { hero ->
        if (hero == null) flowOf(emptyList()) else daily.observeByDate(date, hero.id).map { rows -> rows.map { it.toDomain() } }
    }
    override suspend fun get(id: String): DailyContract? {
        val hero = db.heroDao().getHero() ?: return null
        return daily.getById(id)?.takeIf { it.contract.heroId == hero.id }?.toDomain()
    }
    override suspend fun accept(id: String, date: String): Boolean {
        val hero = db.heroDao().getHero() ?: return false
        val c = daily.getById(id)?.contract ?: return false
        if (c.heroId != hero.id || c.date != date || c.status != ContractStatus.AVAILABLE.name) return false
        val state = remote?.let { runCatching { it.update(id, "ACCEPT") }.getOrNull() }
        if (remote != null && state == null) return false
        return db.withTransaction {
            if (state != null) {
                daily.updateStatus(id, state.status.name, state.acceptedAt, state.completedAt)
                state.status == ContractStatus.ACCEPTED
            } else {
                if (!limit.canAccept(daily.countAcceptedByDate(date, hero.id))) return@withTransaction false
                daily.updateStatus(id, ContractStatus.ACCEPTED.name, System.currentTimeMillis(), null)
                true
            }
        }
    }
    override suspend fun abandon(id: String) {
        val c = get(id) ?: return
        val state = if (c.status == ContractStatus.ACCEPTED) remote?.let { runCatching { it.update(id, "ABANDON") }.getOrNull() } else null
        db.withTransaction {
            if (c.status == ContractStatus.ACCEPTED) {
                if (remote != null) {
                    if (state == null) return@withTransaction
                    daily.updateStatus(id, state.status.name, state.acceptedAt, state.completedAt)
                } else daily.updateStatus(id, ContractStatus.ABANDONED.name, c.acceptedAt, null)
            }
        }
    }
    override suspend fun expireBefore(date: String) { daily.expireBefore(date) }
}
