package com.luminor.tavernquest.domain.usecase.journal
import javax.inject.Inject
import com.luminor.tavernquest.domain.repository.JournalRepository
class GetJournalUseCase @Inject constructor(private val repo:JournalRepository){operator fun invoke()=repo.observeAll()}
