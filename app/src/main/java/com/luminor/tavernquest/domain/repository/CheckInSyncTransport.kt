package com.luminor.tavernquest.domain.repository

import com.luminor.tavernquest.domain.model.CheckIn

sealed interface CheckInSyncResult {
    data class Validated(val officialXp: Int) : CheckInSyncResult
    data class Rejected(val reason: String?) : CheckInSyncResult
    data class Retry(val reason: String) : CheckInSyncResult
}

interface CheckInSyncTransport {
    suspend fun upload(checkIn: CheckIn): CheckInSyncResult
}
