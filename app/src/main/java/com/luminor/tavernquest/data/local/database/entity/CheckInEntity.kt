package com.luminor.tavernquest.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "check_in", indices = [
    Index(value = ["dailyContractId"], unique = true),
    Index(value = ["heroId", "activityDate"]),
    Index(value = ["missionId"]),
])
data class CheckInEntity(
    @PrimaryKey val id: String,
    val dailyContractId: String,
    val missionId: String,
    val heroId: String,
    val notes: String,
    val proofPhotoPath: String?,
    val completedAt: Long,
    val title: String,
    val category: String,
    val xpEarned: Int,
    val startedAt: Long?,
    val durationSeconds: Long,
    val activityDate: String,
    val syncStatus: String,
)
