package com.luminor.tavernquest.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.luminor.tavernquest.domain.model.ContractCategory
import com.luminor.tavernquest.domain.model.ContractDifficulty
import com.luminor.tavernquest.domain.model.ContractStatus
import com.luminor.tavernquest.domain.model.ContractTemplate
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class RemoteMissionOccurrence(
    val occurrenceId: String,
    val template: ContractTemplate,
    val date: String,
    val status: ContractStatus,
    val acceptedAt: Long?,
    val startedAt: Long?,
    val completedAt: Long?,
)

/** Cloud Functions owns mission state. Room receives only this returned projection. */
class FirebaseMissionRemoteRepository(
    private val auth: FirebaseAuth?,
    private val functions: FirebaseFunctions?,
) {
    suspend fun board(date: String): List<RemoteMissionOccurrence> {
        require(auth?.currentUser != null) { "Faça login antes de acessar missões." }
        val raw = await(functionsOrThrow().getHttpsCallable("getDailyMissionBoard").call(mapOf("date" to date))).data as? Map<*, *> ?: emptyMap<Any, Any>()
        val items = raw["items"] as? List<*> ?: emptyList<Any>()
        return items.mapNotNull { (it as? Map<*, *>)?.toOccurrence() }
    }

    suspend fun update(occurrenceId: String, action: String): RemoteMissionOccurrence? {
        require(auth?.currentUser != null) { "Faça login antes de alterar uma missão." }
        val raw = await(functionsOrThrow().getHttpsCallable("updateMissionStatus").call(mapOf("occurrenceId" to occurrenceId, "action" to action))).data as? Map<*, *> ?: return null
        // The callable returns state. Template details remain in the board cache.
        return raw.toStateOnly()
    }

    private fun functionsOrThrow() = requireNotNull(functions) { "Firebase Functions não configurado." }

    private fun Map<*, *>.toOccurrence(): RemoteMissionOccurrence? {
        val mission = this["mission"] as? Map<*, *> ?: return null
        val id = mission["id"] as? String ?: this["missionId"] as? String ?: return null
        val category = runCatching { ContractCategory.valueOf(mission["category"] as? String ?: "") }.getOrNull() ?: return null
        val difficulty = runCatching { ContractDifficulty.valueOf(mission["difficulty"] as? String ?: "") }.getOrNull() ?: return null
        val template = ContractTemplate(id, mission["title"] as? String ?: return null, mission["description"] as? String ?: "", category, difficulty, (mission["xpReward"] as? Number)?.toInt() ?: 0, mission["enabled"] as? Boolean ?: true)
        return RemoteMissionOccurrence(
            occurrenceId = this["occurrenceId"] as? String ?: return null,
            template = template,
            date = this["date"] as? String ?: return null,
            status = runCatching { ContractStatus.valueOf(this["status"] as? String ?: "") }.getOrNull() ?: return null,
            acceptedAt = (this["acceptedAt"] as? Number)?.toLong(),
            startedAt = (this["startedAt"] as? Number)?.toLong(),
            completedAt = (this["completedAt"] as? Number)?.toLong(),
        )
    }

    private fun Map<*, *>.toStateOnly(): RemoteMissionOccurrence? {
        val occurrenceId = this["occurrenceId"] as? String ?: return null
        val status = runCatching { ContractStatus.valueOf(this["status"] as? String ?: "") }.getOrNull() ?: return null
        return RemoteMissionOccurrence(occurrenceId, ContractTemplate("", "", "", ContractCategory.STRENGTH_DISCIPLINE, ContractDifficulty.COMMON, 0), this["date"] as? String ?: "", status, (this["acceptedAt"] as? Number)?.toLong(), (this["startedAt"] as? Number)?.toLong(), (this["completedAt"] as? Number)?.toLong())
    }

    private suspend fun <T> await(task: com.google.android.gms.tasks.Task<T>): T = suspendCancellableCoroutine { continuation ->
        task.addOnCompleteListener { result ->
            if (result.isSuccessful) continuation.resume(result.result)
            else continuation.resumeWith(Result.failure(result.exception ?: IllegalStateException("Falha no Firebase.")))
        }
    }
}
