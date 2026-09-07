package com.luminor.tavernquest.domain.usecase.hero
import com.luminor.tavernquest.domain.model.HeroStatistics
class GetHeroStatisticsUseCase{operator fun invoke(completed:Int,streak:Int)=HeroStatistics(completed,streak)}
