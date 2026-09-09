package com.luminor.tavernquest.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.luminor.tavernquest.domain.repository.PhotoUploadRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

class FirebasePhotoUploadRepository(private val storage: FirebaseStorage?) : PhotoUploadRepository {
    override suspend fun upload(localPath: String, userId: String, checkInId: String): Result<String> {
        val firebase = storage ?: return Result.failure(IllegalStateException("Firebase Storage não configurado."))
        return runCatching {
            val reference = firebase.reference.child("users/$userId/checkins/$checkInId/photo.jpg")
            await(reference.putFile(Uri.fromFile(File(localPath))))
            // Persist only the Storage path. A download URL is a bearer link and must
            // not be copied into shared Firestore documents.
            reference.path
        }
    }

    private suspend fun <T> await(task: com.google.android.gms.tasks.Task<T>): T = suspendCancellableCoroutine { continuation ->
        task.addOnCompleteListener { result ->
            if (result.isSuccessful) continuation.resume(result.result)
            else continuation.resumeWith(Result.failure(result.exception ?: IllegalStateException("Falha no upload da foto.")))
        }
    }
}
