package com.luminor.tavernquest.domain.usecase.contract
import javax.inject.Inject
import com.luminor.tavernquest.core.time.DateProvider;import com.luminor.tavernquest.domain.repository.ContractRepository
class GetDailyBoardUseCase @Inject constructor(private val repo:ContractRepository,private val time:DateProvider){operator fun invoke()=repo.observeBoard(time.today().toString())}
