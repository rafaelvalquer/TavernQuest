package com.luminor.tavernquest.domain.model
data class DailyContract(val id:String,val template:ContractTemplate,val heroId:String,val date:String,val status:ContractStatus,val acceptedAt:Long?=null,val completedAt:Long?=null,val startedAt:Long?=null)
