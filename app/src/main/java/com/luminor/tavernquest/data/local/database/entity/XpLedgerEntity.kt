package com.luminor.tavernquest.data.local.database.entity
import androidx.room.Entity; import androidx.room.PrimaryKey; import androidx.room.Index
@Entity(tableName="xp_ledger",indices=[Index(value=["sourceType","sourceId"],unique=true)]) data class XpLedgerEntity(@PrimaryKey val id:String,val heroId:String,val amount:Int,val sourceType:String,val sourceId:String,val createdAt:Long)
