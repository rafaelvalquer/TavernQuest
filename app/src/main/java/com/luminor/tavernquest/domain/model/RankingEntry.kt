package com.luminor.tavernquest.domain.model

data class RankingEntry(
    val heroId: String,
    val xp: Int,
    val activeDays: Int,
    val checkIns: Int,
)
