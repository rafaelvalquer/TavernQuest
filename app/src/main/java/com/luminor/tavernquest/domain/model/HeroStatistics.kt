package com.luminor.tavernquest.domain.model
data class HeroStatistics(val completed:Int=0,val streak:Int=0,val byCategory:Map<ContractCategory,Int> = emptyMap())
