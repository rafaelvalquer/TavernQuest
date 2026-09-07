package com.luminor.tavernquest.data.local.database.entity
import androidx.room.Entity; import androidx.room.PrimaryKey
@Entity(tableName="tavern") data class TavernEntity(@PrimaryKey val id:String,val name:String,val emblem:String,val createdAt:Long,val description:String="",val isPrivate:Boolean=true)
