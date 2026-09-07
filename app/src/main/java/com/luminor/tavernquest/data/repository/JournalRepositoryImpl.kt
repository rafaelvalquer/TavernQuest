package com.luminor.tavernquest.data.repository
import com.luminor.tavernquest.data.local.database.dao.QuestCompletionDao;import com.luminor.tavernquest.data.mapper.toDomain;import com.luminor.tavernquest.domain.repository.JournalRepository;import kotlinx.coroutines.flow.map
class JournalRepositoryImpl(private val dao:QuestCompletionDao):JournalRepository{override fun observeAll()=dao.observeAll().map{v->v.map{it.toDomain()}};override suspend fun getCompletedDates()=dao.getCompletedDates()}
