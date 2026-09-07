package com.luminor.tavernquest.data.local.database.relation
import androidx.room.*; import com.luminor.tavernquest.data.local.database.entity.*
data class CompletionWithContract(@Embedded val completion:QuestCompletionEntity,@Relation(parentColumn="dailyContractId",entityColumn="id") val contract:DailyContractEntity)
