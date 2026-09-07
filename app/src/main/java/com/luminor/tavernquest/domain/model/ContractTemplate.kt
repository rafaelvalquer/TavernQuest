package com.luminor.tavernquest.domain.model
data class ContractTemplate(val id:String,val title:String,val description:String,val category:ContractCategory,val difficulty:ContractDifficulty,val xpReward:Int,val enabled:Boolean=true)
