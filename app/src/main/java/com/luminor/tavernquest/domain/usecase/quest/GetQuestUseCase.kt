package com.luminor.tavernquest.domain.usecase.quest
import javax.inject.Inject
import com.luminor.tavernquest.domain.repository.ContractRepository
class GetQuestUseCase @Inject constructor(private val repo:ContractRepository){suspend operator fun invoke(id:String)=repo.get(id)}
