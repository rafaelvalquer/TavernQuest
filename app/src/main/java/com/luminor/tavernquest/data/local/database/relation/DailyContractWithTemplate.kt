package com.luminor.tavernquest.data.local.database.relation
import androidx.room.*; import com.luminor.tavernquest.data.local.database.entity.*
data class DailyContractWithTemplate(@Embedded val contract:DailyContractEntity,@Relation(parentColumn="templateId",entityColumn="id") val template:ContractTemplateEntity)
