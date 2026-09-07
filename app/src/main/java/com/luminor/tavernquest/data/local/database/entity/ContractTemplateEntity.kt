package com.luminor.tavernquest.data.local.database.entity
import androidx.room.Entity; import androidx.room.PrimaryKey
@Entity(tableName="contract_template") data class ContractTemplateEntity(@PrimaryKey val id:String,val title:String,val description:String,val category:String,val difficulty:String,val xpReward:Int,val enabled:Boolean)
