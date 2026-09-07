package com.luminor.tavernquest.data.local.database.entity
import androidx.room.Entity; import androidx.room.PrimaryKey
@Entity(tableName="hero") data class HeroEntity(@PrimaryKey val id:String,val name:String,val appearance:String,val heroClass:String,val totalXp:Int,val createdAt:Long,val userId:String=id)
