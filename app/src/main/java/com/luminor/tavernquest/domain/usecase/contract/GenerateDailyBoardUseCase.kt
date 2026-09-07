package com.luminor.tavernquest.domain.usecase.contract
import javax.inject.Inject
import com.luminor.tavernquest.core.time.DateProvider;import com.luminor.tavernquest.domain.repository.*
class GenerateDailyBoardUseCase @Inject constructor(private val contracts:ContractRepository,private val taverns:TavernRepository,private val time:DateProvider){suspend operator fun invoke(){val t=taverns.get()?:return;contracts.expireBefore(time.today().toString());contracts.generateBoard(time.today().toString(),t.id)}}
