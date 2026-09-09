package com.luminor.tavernquest.data.repository
import androidx.room.withTransaction
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import com.luminor.tavernquest.data.local.database.entity.TavernMemberEntity
import com.luminor.tavernquest.data.mapper.*
import com.luminor.tavernquest.core.util.TavernInviteCode
import com.luminor.tavernquest.domain.model.*
import com.luminor.tavernquest.domain.repository.TavernRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class TavernRepositoryImpl(private val db:TavernQuestDatabase, private val remote: com.luminor.tavernquest.domain.repository.TavernRemoteRepository? = null):TavernRepository {
    override suspend fun create(tavern:Tavern,owner:TavernMember){ val authoritative=remote?.create(tavern,owner)?.getOrThrow()?:tavern; db.withTransaction { db.tavernDao().insert(authoritative.toEntity()); db.tavernMemberDao().insert(TavernMemberEntity(owner.id,authoritative.id,owner.heroId,owner.role.name,authoritative.createdAt)) } }
    override suspend fun get()=db.tavernDao().getTavern()?.toDomain()
    override suspend fun getById(id:String)=db.tavernDao().getById(id)?.toDomain()
    override suspend fun memberCount(tavernId:String)=db.tavernMemberDao().getMembers(tavernId).size
    override suspend fun members(tavernId:String)=db.tavernMemberDao().getMembers(tavernId).map { TavernMember(it.id,it.tavernId,it.heroId,runCatching { TavernRole.valueOf(it.role) }.getOrDefault(TavernRole.MEMBER),it.joinedAt) }
    override fun observeMembers(tavernId: String) = combine(
        db.tavernMemberDao().observeMembers(tavernId).map { values -> values.map { TavernMember(it.id, it.tavernId, it.heroId, runCatching { TavernRole.valueOf(it.role) }.getOrDefault(TavernRole.MEMBER), it.joinedAt) } },
        remote?.observeMembers(tavernId) ?: kotlinx.coroutines.flow.flowOf(emptyList()),
    ) { local, online -> if (online.isEmpty()) local else online }
    override fun observe()=db.tavernDao().observeTavern().map{it?.toDomain()}
    override fun observeForHero(heroId:String)=combine(
        db.tavernDao().observeForHero(heroId).map { list -> list.map { it.toDomain() } },
        remote?.observeTaverns()?.onEach { values -> values.forEach { db.tavernDao().insert(it.toEntity()) } } ?: kotlinx.coroutines.flow.flowOf(emptyList()),
    ) { local, online -> (local + online).distinctBy { it.id }.sortedBy { it.createdAt } }
    // Until invite codes become a separate server-owned table, the immutable tavern id is the local invite code.
    override suspend fun getByCode(code:String): Tavern? {
        val normalized = code.trim().uppercase()
        db.tavernDao().getAll().firstOrNull { TavernInviteCode.fromId(it.id).equals(normalized, ignoreCase = true) }?.toDomain()?.let { return it }
        val remoteTavern = remote?.findByInviteCode(normalized)?.getOrNull() ?: return null
        db.tavernDao().insert(remoteTavern.toEntity())
        return remoteTavern
    }
    override suspend fun join(tavernId:String,member:TavernMember):Boolean { val tavern=db.tavernDao().getById(tavernId)?.toDomain() ?: return false; remote?.join(tavern,member)?.getOrElse { return false }; return db.withTransaction { if(db.tavernMemberDao().getForHero(tavernId,member.heroId)!=null)return@withTransaction true;db.tavernMemberDao().insert(TavernMemberEntity(member.id,tavernId,member.heroId,TavernRole.MEMBER.name,member.joinedAt));true } }
    override suspend fun leave(tavernId:String,heroId:String){ remote?.leave(tavernId)?.getOrThrow(); db.tavernMemberDao().leave(tavernId,heroId) }
}
