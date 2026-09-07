package com.luminor.tavernquest.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luminor.tavernquest.domain.model.Tavern
import com.luminor.tavernquest.domain.model.TavernMember
import com.luminor.tavernquest.domain.repository.TavernRemoteRepository
import com.luminor.tavernquest.domain.model.TavernEmblem
import com.luminor.tavernquest.core.util.TavernInviteCode
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseTavernRemoteRepository(private val firestore: FirebaseFirestore?, private val auth: FirebaseAuth?) : TavernRemoteRepository {
    override suspend fun create(tavern: Tavern, owner: TavernMember): Result<Unit> {
        val db = firestore ?: return Result.failure(missingConfiguration())
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Usuário não autenticado."))
        return runCatching {
            val tavernRef = db.collection("taverns").document(tavern.id)
            val memberRef = tavernRef.collection("members").document(user.uid)
            val inviteCode = TavernInviteCode.fromId(tavern.id)
            val inviteRef = db.collection("invites").document(inviteCode)
            val batch = db.batch()
            val publicData = mapOf("tavernId" to tavern.id, "name" to tavern.name, "description" to tavern.description, "emblem" to tavern.emblem.name, "createdAt" to tavern.createdAt, "private" to tavern.isPrivate)
            batch.set(tavernRef, publicData + mapOf("id" to tavern.id, "ownerId" to user.uid, "inviteCode" to inviteCode))
            batch.set(inviteRef, publicData + mapOf("ownerId" to user.uid, "code" to inviteCode))
            batch.set(memberRef, mapOf("userId" to user.uid, "heroId" to owner.heroId, "role" to "OWNER", "joinedAt" to owner.joinedAt))
            await(batch.commit())
        }
    }

    override suspend fun findByInviteCode(code: String): Result<Tavern?> {
        val db = firestore ?: return Result.failure(missingConfiguration())
        auth?.currentUser ?: return Result.failure(IllegalStateException("Usuário não autenticado."))
        return runCatching {
            val invite = await(db.collection("invites").document(code.trim().uppercase()).get())
            if (!invite.exists()) null else Tavern(
                id = invite.getString("tavernId") ?: error("Convite inválido."),
                name = invite.getString("name") ?: "Taberna",
                emblem = invite.getString("emblem")?.let { runCatching { TavernEmblem.valueOf(it) }.getOrNull() } ?: TavernEmblem.WOLF,
                createdAt = invite.getLong("createdAt") ?: 0L,
                description = invite.getString("description") ?: "",
                isPrivate = invite.getBoolean("private") ?: true,
            )
        }
    }

    override suspend fun join(tavern: Tavern, member: TavernMember): Result<Unit> {
        val db = firestore ?: return Result.failure(missingConfiguration())
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Usuário não autenticado."))
        return runCatching {
            val ref = db.collection("taverns").document(tavern.id).collection("members").document(user.uid)
            await(ref.set(mapOf("userId" to user.uid, "heroId" to member.heroId, "role" to "MEMBER", "joinedAt" to member.joinedAt)))
        }
    }

    override suspend fun leave(tavernId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(missingConfiguration())
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Usuário não autenticado."))
        return runCatching { await(db.collection("taverns").document(tavernId).collection("members").document(user.uid).delete()) }
    }

    private suspend fun <T> await(task: com.google.android.gms.tasks.Task<T>): T = suspendCancellableCoroutine { continuation ->
        task.addOnCompleteListener { result ->
            if (result.isSuccessful) continuation.resume(result.result)
            else continuation.resumeWith(Result.failure(result.exception ?: IllegalStateException("Falha no Firestore.")))
        }
    }

    private fun missingConfiguration() = IllegalStateException("Firebase Firestore não configurado.")
}
