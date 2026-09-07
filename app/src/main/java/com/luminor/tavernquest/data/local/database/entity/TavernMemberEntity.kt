package com.luminor.tavernquest.data.local.database.entity
import androidx.room.Entity; import androidx.room.PrimaryKey
@Entity(tableName="tavern_member") data class TavernMemberEntity(@PrimaryKey val id:String,val tavernId:String,val heroId:String,val role:String,val joinedAt:Long)
