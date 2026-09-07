package com.luminor.tavernquest.domain.usecase.contract
import javax.inject.Inject
import com.luminor.tavernquest.domain.model.ContractStatus
import kotlinx.coroutines.flow.map
class GetActiveContractsUseCase @Inject constructor(private val board:GetDailyBoardUseCase){operator fun invoke()=board().map{list->list.filter{it.status==ContractStatus.ACCEPTED}}}
