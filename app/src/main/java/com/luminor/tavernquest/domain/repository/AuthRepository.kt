package com.luminor.tavernquest.domain.repository

import com.luminor.tavernquest.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    suspend fun loginWithGoogleIdToken(idToken: String): Result<AuthUser>
    /** Creates the server account document before any hero or device-token operation. */
    suspend fun ensureAccount(): Result<Unit>
    fun logout()
}
