package com.luminor.tavernquest.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.luminor.tavernquest.domain.model.AuthUser
import com.luminor.tavernquest.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseAuthRepository(private val auth: FirebaseAuth?) : AuthRepository {
    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val firebase = auth
        if (firebase == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toDomain()) }
        firebase.addAuthStateListener(listener)
        trySend(firebase.currentUser?.toDomain())
        awaitClose { firebase.removeAuthStateListener(listener) }
    }


    override suspend fun loginWithGoogleIdToken(idToken: String): Result<AuthUser> {
        val firebase = auth ?: return Result.failure(missingConfiguration())
        return runCatching { await(firebase.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))).user?.toDomain() ?: error("Usuário não retornado.") }
    }

    override fun logout() { auth?.signOut() }

    private suspend fun <T> await(task: com.google.android.gms.tasks.Task<T>): T = suspendCancellableCoroutine { continuation ->
        task.addOnCompleteListener { result ->
            if (result.isSuccessful) continuation.resume(result.result)
            else continuation.resumeWith(Result.failure(result.exception ?: IllegalStateException("Falha na operação Firebase.")))
        }
    }

    private fun missingConfiguration() = IllegalStateException("Firebase não configurado: adicione google-services.json em app/.")
}

private fun com.google.firebase.auth.FirebaseUser.toDomain() = AuthUser(uid, email, displayName)
