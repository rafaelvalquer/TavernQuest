package com.luminor.tavernquest.data.mapper
import com.luminor.tavernquest.data.local.database.entity.XpLedgerEntity;import com.luminor.tavernquest.domain.model.XpTransaction
fun XpLedgerEntity.toDomain()=XpTransaction(id,heroId,amount,sourceType,sourceId,createdAt)
