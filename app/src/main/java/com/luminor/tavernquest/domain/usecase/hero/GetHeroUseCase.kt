package com.luminor.tavernquest.domain.usecase.hero
import javax.inject.Inject
import com.luminor.tavernquest.domain.repository.HeroRepository
class GetHeroUseCase @Inject constructor(private val repo:HeroRepository){operator fun invoke()=repo.observe()}
