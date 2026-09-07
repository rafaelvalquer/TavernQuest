package com.luminor.tavernquest.data.repository
import androidx.room.withTransaction
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import com.luminor.tavernquest.data.local.database.entity.TavernMemberEntity
import com.luminor.tavernquest.data.mapper.*
import com.luminor.tavernquest.core.util.TavernInviteCode
import com.luminor.tavernquest.domain.model.*
import com.luminor.tavernquest.domain.repository.TavernRepository
import kotlinx.coroutines.flow.map

class TavernRepositoryImpl(private val db:TavernQuestDatabase):TavernRepository {
    override suspend fun create(tavern:Tavern,owner:TavernMember)=db.withTransaction { db.tavernDao().insert(tavern.toEntity()); db.tavernMemberDao().insert(TavernMemberEntity(owner.id,owner.tavernId,owner.heroId,owner.role.name,owner.joinedAt)) }
    override suspend fun get()=db.tavernDao().getTavern()?.toDomain()
    override suspend fun getById(id:String)=db.tavernDao().getById(id)?.toDomain()
    override suspend fun memberCount(tavernId:String)=db.tavernMemberDao().getMembers(tavernId).size
    override fun observe()=db.tavernDao().observeTavern().map{it?.toDomain()}
    override fun observeForHero(heroId:String)=db.tavernDao().observeForHero(heroId).map{list->list.map{it.toDomain()}}
    // Until invite codes become a separate server-owned table, the immutable tavern id is the local invite code.
    override suspend fun getByCode(code:String)=db.tavernDao().getAll().firstOrNull { TavernInviteCode.fromId(it.id).equals(code.trim().uppercase(), ignoreCase = true) }?.toDomain()
    override suspend fun join(tavernId:String,member:TavernMember):Boolean=db.withTransaction { if(db.tavernDao().getById(tavernId)==null)return@withTransaction false;if(db.tavernMemberDao().getForHero(tavernId,member.heroId)!=null)return@withTransaction true;db.tavernMemberDao().insert(TavernMemberEntity(member.id,tavernId,member.heroId,TavernRole.MEMBER.name,member.joinedAt));true }
    override suspend fun leave(tavernId:String,heroId:String)=db.tavernMemberDao().leave(tavernId,heroId)
}
