package com.luminor.tavernquest.data.remote.firebase

import com.luminor.tavernquest.domain.model.CheckIn

/** Payload sent by the client. XP is deliberately zero; Functions assign the official value. */
data class FirebaseCheckInPayload(
    val id: String,
    val userId: String,
    val missionId: String,
    val occurrenceId: String,
    val missionTitle: String,
    val category: String,
    val startedAt: Long?,
    val completedAt: Long,
    val durationSeconds: Long,
    val notes: String?,
    val photoUrl: String?,
    val createdAt: Long,
    val xpEarned: Int = 0,
    val status: String = "PENDING_SYNC",
) {
    companion object {
        fun from(checkIn: CheckIn, userId: String = checkIn.heroId) = FirebaseCheckInPayload(
            id = checkIn.id,
            userId = userId,
            missionId = checkIn.missionId,
            occurrenceId = checkIn.occurrenceId,
            missionTitle = checkIn.title,
            category = checkIn.category.name,
            startedAt = checkIn.startedAt,
            completedAt = checkIn.completedAt,
            durationSeconds = checkIn.durationSeconds,
            notes = checkIn.notes,
            photoUrl = checkIn.proofPhotoUrl,
            createdAt = checkIn.completedAt,
        )
    }
}
