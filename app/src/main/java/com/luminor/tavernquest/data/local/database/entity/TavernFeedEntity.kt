package com.luminor.tavernquest.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** A lightweight publication reference. The canonical activity remains in check_in. */
@Entity(
    tableName = "tavern_feed",
    primaryKeys = ["tavernId", "checkInId"],
    foreignKeys = [ForeignKey(entity = CheckInEntity::class, parentColumns = ["id"], childColumns = ["checkInId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["checkInId"])]
)
data class TavernFeedEntity(
    val tavernId: String,
    val checkInId: String,
    val publishedAt: Long,
)
