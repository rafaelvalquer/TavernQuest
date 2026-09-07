package com.luminor.tavernquest.data.local.database.entity
import androidx.room.Entity; import androidx.room.PrimaryKey; import androidx.room.Index
@Entity(tableName="daily_contract",indices=[Index(value=["date"]),Index(value=["heroId"])]) data class DailyContractEntity(@PrimaryKey val id:String,val templateId:String,val heroId:String,val date:String,val status:String,val acceptedAt:Long?,val completedAt:Long?,val startedAt:Long?=null)
