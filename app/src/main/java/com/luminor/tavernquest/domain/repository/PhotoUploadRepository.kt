package com.luminor.tavernquest.domain.repository

interface PhotoUploadRepository {
    suspend fun upload(localPath: String, userId: String, checkInId: String): Result<String>
}
