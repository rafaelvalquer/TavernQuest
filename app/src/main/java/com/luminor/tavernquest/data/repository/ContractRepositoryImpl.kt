package com.luminor.tavernquest.data.repository
import androidx.room.withTransaction
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import com.luminor.tavernquest.data.local.database.entity.DailyContractEntity
import com.luminor.tavernquest.data.mapper.*
import com.luminor.tavernquest.data.seed.ContractSeed
import com.luminor.tavernquest.domain.model.*
import com.luminor.tavernquest.domain.repository.ContractRepository
import com.luminor.tavernquest.domain.rules.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class ContractRepositoryImpl(private val db: TavernQuestDatabase) : ContractRepository {
    private val templates = db.contractTemplateDao()
    private val daily = db.dailyContractDao()
    private val gen = DailyBoardGenerator()
    private val limit = ContractLimitRule()
    override suspend fun ensureSeeded() { if (templates.count() == 0) templates.insertAll(ContractSeed.all.map { it.toEntity() }) }
    override suspend fun generateBoard(date: String, heroId: String) = db.withTransaction {
        ensureSeeded()
        if (daily.getByDate(date, heroId).any { it.contract.date == date }) return@withTransaction
        val chosen = gen.generate(templates.getEnabled().map { it.toDomain() }, date)
        daily.insertAll(chosen.map { DailyContractEntity("$heroId:$date:${it.id}", it.id, heroId, date, ContractStatus.AVAILABLE.name, null, null) })
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
    override suspend fun accept(id: String, date: String): Boolean = db.withTransaction {
        val hero = db.heroDao().getHero() ?: return@withTransaction false
        val c = daily.getById(id)?.contract ?: return@withTransaction false
        if (c.heroId != hero.id || c.date != date || c.status != ContractStatus.AVAILABLE.name) return@withTransaction false
        if (!limit.canAccept(daily.countAcceptedByDate(date, hero.id))) return@withTransaction false
        daily.updateStatus(id, ContractStatus.ACCEPTED.name, System.currentTimeMillis(), null)
        true
    }
    override suspend fun abandon(id: String) = db.withTransaction {
        val c = get(id) ?: return@withTransaction
        if (c.status == ContractStatus.ACCEPTED) daily.updateStatus(id, ContractStatus.ABANDONED.name, c.acceptedAt, null)
    }
    override suspend fun expireBefore(date: String) { daily.expireBefore(date) }
}
