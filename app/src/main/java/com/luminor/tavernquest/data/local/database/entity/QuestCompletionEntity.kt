package com.luminor.tavernquest.data.local.database.entity
import androidx.room.Entity; import androidx.room.PrimaryKey; import androidx.room.Index
@Entity(tableName="quest_completion",indices=[Index(value=["dailyContractId"],unique=true)]) data class QuestCompletionEntity(@PrimaryKey val id:String,val dailyContractId:String,val heroId:String,val notes:String,val proofPhotoPath:String?,val completedAt:Long)
