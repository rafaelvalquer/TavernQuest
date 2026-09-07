package com.luminor.tavernquest.domain.usecase.hero
import javax.inject.Inject
import com.luminor.tavernquest.domain.rules.LevelCalculator
class GetHeroProgressUseCase @Inject constructor(private val calc:LevelCalculator){operator fun invoke(xp:Int)=calc.calculate(xp)}
