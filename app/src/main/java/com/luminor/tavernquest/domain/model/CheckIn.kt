package com.luminor.tavernquest.domain.model

enum class SyncStatus { PENDING_SYNC, VALIDATED, SYNCED, REJECTED, LOCAL_ONLY }

data class CheckIn(
    val id: String,
    val heroId: String,
    val missionId: String,
    val title: String,
    val category: ContractCategory,
    val xpEarned: Int,
    val startedAt: Long?,
    val completedAt: Long,
    val durationSeconds: Long,
    val activityDate: String,
    val notes: String?,
    val proofPhotoUrl: String?,
    val syncStatus: SyncStatus,
    /** Stable daily-contract occurrence; mission IDs are reusable on later days. */
    val occurrenceId: String = id,
)
