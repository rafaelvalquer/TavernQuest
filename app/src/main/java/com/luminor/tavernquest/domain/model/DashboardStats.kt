package com.luminor.tavernquest.domain.model

data class DashboardStats(
    val totalCheckIns: Int = 0,
    val totalXp: Int = 0,
    val activeDays: Int = 0,
    val activeSeconds: Long = 0,
)

data class ActivityDay(
    val date: String,
    val count: Int,
    val xp: Int,
    val activeSeconds: Long,
    val category: ContractCategory?,
)
