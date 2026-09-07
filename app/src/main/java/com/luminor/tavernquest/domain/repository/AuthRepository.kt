package com.luminor.tavernquest.domain.repository

import com.luminor.tavernquest.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    suspend fun register(email: String, password: String, displayName: String?): Result<AuthUser>
    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun loginWithGoogleIdToken(idToken: String): Result<AuthUser>
    fun logout()
}
