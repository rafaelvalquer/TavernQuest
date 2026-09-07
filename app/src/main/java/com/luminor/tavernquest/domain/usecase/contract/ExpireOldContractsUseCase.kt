package com.luminor.tavernquest.domain.usecase.contract
import javax.inject.Inject
import com.luminor.tavernquest.core.time.DateProvider;import com.luminor.tavernquest.domain.repository.ContractRepository
class ExpireOldContractsUseCase @Inject constructor(private val repo:ContractRepository,private val time:DateProvider){suspend operator fun invoke()=repo.expireBefore(time.today().toString())}
