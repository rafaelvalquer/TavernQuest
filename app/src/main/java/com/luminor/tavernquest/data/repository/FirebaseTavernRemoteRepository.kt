package com.luminor.tavernquest.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luminor.tavernquest.domain.model.Tavern
import com.luminor.tavernquest.domain.model.TavernMember
import com.luminor.tavernquest.domain.repository.TavernRemoteRepository
import com.luminor.tavernquest.domain.model.TavernEmblem
import com.luminor.tavernquest.domain.model.CheckIn
import com.luminor.tavernquest.domain.model.ContractCategory
import com.luminor.tavernquest.domain.model.RankingEntry
import com.luminor.tavernquest.domain.model.SyncStatus
import com.luminor.tavernquest.core.util.TavernInviteCode
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

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
            await(ref.set(mapOf("userId" to user.uid, "heroId" to member.heroId, "role" to "MEMBER", "joinedAt" to member.joinedAt, "inviteCode" to TavernInviteCode.fromId(tavern.id))))
        }
    }

    override suspend fun leave(tavernId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(missingConfiguration())
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Usuário não autenticado."))
        return runCatching { await(db.collection("taverns").document(tavernId).collection("members").document(user.uid).delete()) }
    }

    override fun observeTaverns(): Flow<List<Tavern>> = callbackFlow {
        val db = firestore ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val user = auth?.currentUser ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val registration = db.collectionGroup("members").whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                launch {
                    val taverns = snapshot?.documents.orEmpty().mapNotNull { membership ->
                        val tavernId = membership.reference.parent.parent?.id ?: return@mapNotNull null
                        runCatching { await(db.collection("taverns").document(tavernId).get()) }.getOrNull()
                            ?.takeIf { it.exists() }?.toTavern()
                    }
                    trySend(taverns)
                }
            }
        awaitClose { registration.remove() }
    }

    override fun observeMembers(tavernId: String): Flow<List<TavernMember>> = callbackFlow {
        val db = firestore ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val registration = db.collection("taverns").document(tavernId).collection("members")
            .orderBy("joinedAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.documents.orEmpty().map { member ->
                    TavernMember(
                        id = member.id,
                        tavernId = tavernId,
                        heroId = member.getString("heroId") ?: member.getString("userId") ?: member.id,
                        role = member.getString("role")?.let { runCatching { com.luminor.tavernquest.domain.model.TavernRole.valueOf(it) }.getOrNull() } ?: com.luminor.tavernquest.domain.model.TavernRole.MEMBER,
                        joinedAt = member.getLong("joinedAt") ?: 0L,
                    )
                })
            }
        awaitClose { registration.remove() }
    }

    override fun observeFeed(tavernId: String): Flow<List<CheckIn>> = callbackFlow {
        val db = firestore ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val registration = db.collection("taverns").document(tavernId).collection("feed")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                launch {
                    val checkIns = snapshot?.documents.orEmpty().mapNotNull { publication ->
                        val checkIn = runCatching { await(db.collection("checkins").document(publication.id).get()) }.getOrNull()
                        checkIn?.takeIf { it.exists() && it.getString("status") == "VALIDATED" }?.toCheckIn()
                    }
                    trySend(checkIns)
                }
            }
        awaitClose { registration.remove() }
    }

    override fun observeRanking(tavernId: String, periodStart: Long?): Flow<List<RankingEntry>> = callbackFlow {
        val db = firestore ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val period = periodKey(periodStart)
        val registration = db.collection("taverns").document(tavernId).collection("leaderboards").document(period)
            .collection("entries").orderBy("xp", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.documents.orEmpty().map { entry ->
                    RankingEntry(
                        heroId = entry.getString("userId") ?: entry.id,
                        xp = (entry.getLong("xp") ?: 0L).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(),
                        activeDays = (entry.getLong("activeDays") ?: 0L).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(),
                        checkIns = (entry.getLong("checkIns") ?: 0L).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(),
                    )
                })
            }
        awaitClose { registration.remove() }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toCheckIn(): CheckIn {
        val completedAt = getLong("completedAt") ?: 0L
        return CheckIn(
            id = getString("id") ?: id,
            heroId = getString("userId") ?: "",
            missionId = getString("missionId") ?: "",
            title = getString("missionTitle") ?: "Missão",
            category = getString("category")?.let { runCatching { ContractCategory.valueOf(it) }.getOrNull() } ?: ContractCategory.HOME_COMMUNITY,
            xpEarned = (getLong("xpEarned") ?: 0L).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(),
            startedAt = getLong("startedAt"),
            completedAt = completedAt,
            durationSeconds = getLong("durationSeconds") ?: 0L,
            activityDate = Instant.ofEpochMilli(completedAt).atZone(ZoneOffset.UTC).toLocalDate().toString(),
            notes = getString("notes"),
            proofPhotoUrl = getString("photoUrl"),
            syncStatus = SyncStatus.VALIDATED,
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toTavern(): Tavern = Tavern(
        id = getString("id") ?: id,
        name = getString("name") ?: "Taberna",
        emblem = getString("emblem")?.let { runCatching { TavernEmblem.valueOf(it) }.getOrNull() } ?: TavernEmblem.WOLF,
        createdAt = getLong("createdAt") ?: 0L,
        description = getString("description") ?: "",
        isPrivate = getBoolean("private") ?: true,
    )

    private fun periodKey(periodStart: Long?): String {
        if (periodStart == null || periodStart <= 0L) return "all"
        val now = System.currentTimeMillis()
        if (now - periodStart >= 15L * 24 * 60 * 60 * 1000) {
            val date = Instant.ofEpochMilli(now).atZone(ZoneOffset.UTC).toLocalDate()
            return "month-${date.year}-${date.monthValue.toString().padStart(2, '0')}"
        }
        val date = Instant.ofEpochMilli(now).atZone(ZoneOffset.UTC).toLocalDate()
        val januaryFirst = LocalDate.of(date.year, 1, 1)
        val sundayBasedOffset = januaryFirst.dayOfWeek.value % 7
        val week = (date.dayOfYear - 1 + sundayBasedOffset + 7) / 7
        return "week-${date.year}-W${week.toString().padStart(2, '0')}"
    }

    private suspend fun <T> await(task: com.google.android.gms.tasks.Task<T>): T = suspendCancellableCoroutine { continuation ->
        task.addOnCompleteListener { result ->
            if (result.isSuccessful) continuation.resume(result.result)
            else continuation.resumeWith(Result.failure(result.exception ?: IllegalStateException("Falha no Firestore.")))
        }
    }

    private fun missingConfiguration() = IllegalStateException("Firebase Firestore não configurado.")
}
