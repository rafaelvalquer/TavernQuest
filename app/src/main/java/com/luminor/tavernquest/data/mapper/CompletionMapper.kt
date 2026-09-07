package com.luminor.tavernquest.data.mapper
import com.luminor.tavernquest.data.local.database.entity.QuestCompletionEntity;import com.luminor.tavernquest.domain.model.QuestCompletion
fun QuestCompletionEntity.toDomain()=QuestCompletion(id,dailyContractId,heroId,notes,proofPhotoPath,completedAt)
