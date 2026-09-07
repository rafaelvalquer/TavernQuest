package com.luminor.tavernquest.domain.rules
class ContractLimitRule(private val max:Int=5){fun canAccept(current:Int)=current<max; fun remaining(current:Int)=(max-current).coerceAtLeast(0)}
