package com.luminor.tavernquest.data.local.database.relation
import androidx.room.*; import com.luminor.tavernquest.data.local.database.entity.*
data class TavernWithMembers(@Embedded val tavern:TavernEntity,@Relation(parentColumn="id",entityColumn="tavernId") val members:List<TavernMemberEntity>)
