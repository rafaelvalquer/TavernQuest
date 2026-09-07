package com.luminor.tavernquest.domain.rules
import com.luminor.tavernquest.domain.model.HeroProgress
class LevelCalculator { fun calculate(totalXp:Int):HeroProgress { var level=1; var left=totalXp.coerceAtLeast(0); var need=100; while(left>=need){left-=need; level++; need=100+(level-1)*25}; return HeroProgress(level,totalXp,left,need,(left.toFloat()/need).coerceIn(0f,1f)) } }
