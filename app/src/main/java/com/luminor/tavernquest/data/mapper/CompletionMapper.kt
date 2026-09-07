package com.luminor.tavernquest.data.mapper
import com.luminor.tavernquest.data.local.database.entity.QuestCompletionEntity;import com.luminor.tavernquest.data.local.database.entity.CheckInEntity;import com.luminor.tavernquest.domain.model.*
fun QuestCompletionEntity.toDomain()=QuestCompletion(id,dailyContractId,heroId,notes,proofPhotoPath,completedAt)
fun CheckInEntity.toCheckIn()=CheckIn(id,missionId,heroId,title,runCatching{ContractCategory.valueOf(category)}.getOrDefault(ContractCategory.STRENGTH_DISCIPLINE),xpEarned,startedAt,completedAt,durationSeconds,activityDate,notes,proofPhotoPath,runCatching{SyncStatus.valueOf(syncStatus)}.getOrDefault(SyncStatus.LOCAL_ONLY))
