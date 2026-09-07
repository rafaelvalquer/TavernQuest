package com.luminor.tavernquest.data.repository
import com.luminor.tavernquest.data.local.database.dao.XpLedgerDao;import com.luminor.tavernquest.domain.repository.XpRepository
class XpRepositoryImpl(private val dao:XpLedgerDao):XpRepository{override fun observeTotal(heroId:String)=dao.observeHeroXp(heroId);override suspend fun total(heroId:String)=dao.getTotalXp(heroId)}
