package com.luminor.tavernquest.domain.usecase.contract
import javax.inject.Inject
import com.luminor.tavernquest.domain.repository.ContractRepository
class AbandonContractUseCase @Inject constructor(private val repo:ContractRepository){suspend operator fun invoke(id:String)=repo.abandon(id)}
