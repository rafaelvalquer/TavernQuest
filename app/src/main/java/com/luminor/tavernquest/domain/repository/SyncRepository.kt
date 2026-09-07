package com.luminor.tavernquest.domain.repository

interface SyncRepository {
    suspend fun pendingCount(): Int
    suspend fun markValidated(checkInId: String, officialXp: Int)
    suspend fun markRejected(checkInId: String, error: String?)
    suspend fun markFailed(checkInId: String, error: String?)
}
