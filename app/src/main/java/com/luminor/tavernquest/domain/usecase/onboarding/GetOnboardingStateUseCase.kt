package com.luminor.tavernquest.domain.usecase.onboarding
import javax.inject.Inject
import com.luminor.tavernquest.domain.repository.SettingsRepository
class GetOnboardingStateUseCase @Inject constructor(private val repo:SettingsRepository){operator fun invoke()=repo.observe()}
