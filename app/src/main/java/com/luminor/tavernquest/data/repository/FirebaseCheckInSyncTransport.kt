package com.luminor.tavernquest.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luminor.tavernquest.data.remote.firebase.FirebaseCheckInPayload
import com.luminor.tavernquest.domain.model.CheckIn
import com.luminor.tavernquest.domain.repository.CheckInSyncResult
import com.luminor.tavernquest.domain.repository.CheckInSyncTransport
import com.luminor.tavernquest.domain.repository.PhotoUploadRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseCheckInSyncTransport(
    private val firestore: FirebaseFirestore?,
    private val auth: FirebaseAuth?,
    private val photos: PhotoUploadRepository,
) : CheckInSyncTransport {
    override suspend fun upload(checkIn: CheckIn): CheckInSyncResult {
        val db = firestore ?: return CheckInSyncResult.Retry("Firebase Firestore não configurado.")
        val user = auth?.currentUser ?: return CheckInSyncResult.Retry("Usuário não autenticado.")
        return runCatching {
            val photoResult = checkIn.proofPhotoUrl?.let { path -> photos.upload(path, user.uid, checkIn.id) }
            if (photoResult != null && photoResult.isFailure) return@runCatching CheckInSyncResult.Retry(photoResult.exceptionOrNull()?.message ?: "Falha no upload da foto.")
            val photoUrl = photoResult?.getOrNull()
            val payload = FirebaseCheckInPayload.from(checkIn, user.uid)
            val values = mapOf(
                "id" to payload.id,
                "userId" to payload.userId,
                "missionId" to payload.missionId,
                "missionTitle" to payload.missionTitle,
                "category" to payload.category,
                "startedAt" to payload.startedAt,
                "completedAt" to payload.completedAt,
                "durationSeconds" to payload.durationSeconds,
                "notes" to payload.notes,
                "photoUrl" to photoUrl,
                "createdAt" to payload.createdAt,
                "xpEarned" to 0,
                "status" to payload.status,
            )
            val reference = db.collection("checkins").document(payload.id)
            val beforeWrite = await(reference.get())
            when (beforeWrite.getString("status")) {
                "VALIDATED" -> return@runCatching CheckInSyncResult.Validated(beforeWrite.getLong("xpEarned")?.toInt() ?: 0)
                "REJECTED" -> return@runCatching CheckInSyncResult.Rejected(beforeWrite.getString("rejectionReason"))
                "PENDING_SYNC" -> return@runCatching CheckInSyncResult.Retry("A validação do servidor ainda está pendente.")
            }
            await(reference.set(values))
            val remote = await(reference.get())
            when (remote.getString("status")) {
                "VALIDATED" -> CheckInSyncResult.Validated(remote.getLong("xpEarned")?.toInt() ?: 0)
                "REJECTED" -> CheckInSyncResult.Rejected(remote.getString("rejectionReason"))
                else -> CheckInSyncResult.Retry("A validação do servidor ainda está pendente.")
            }
        }.getOrElse { CheckInSyncResult.Retry(it.message ?: "Falha ao sincronizar check-in.") }
    }

    private suspend fun <T> await(task: com.google.android.gms.tasks.Task<T>): T = suspendCancellableCoroutine { continuation ->
        task.addOnCompleteListener { result ->
            if (result.isSuccessful) continuation.resume(result.result)
            else continuation.resumeWith(Result.failure(result.exception ?: IllegalStateException("Falha no Firebase.")))
        }
    }
}
