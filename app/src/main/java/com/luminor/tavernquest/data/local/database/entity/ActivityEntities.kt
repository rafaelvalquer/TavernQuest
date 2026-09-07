package com.luminor.tavernquest.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val heroId: String,
    val totalCheckIns: Int,
    val totalXp: Int,
    val activeDays: Int,
    val activeSeconds: Long,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
)

@Entity(tableName = "user_activity_day", primaryKeys = ["heroId", "date"])
data class UserActivityDayEntity(
    val heroId: String,
    val date: String,
    val checkInCount: Int,
    val totalXp: Int,
    val activeSeconds: Long,
    val primaryCategory: String?,
    val thumbnailUrl: String?,
)

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey val checkInId: String,
    val createdAt: Long,
    val attempts: Int = 0,
    val lastError: String? = null,
)
