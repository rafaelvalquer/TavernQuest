package com.luminor.tavernquest.domain.usecase.onboarding
import javax.inject.Inject
import com.luminor.tavernquest.core.time.DateProvider;import com.luminor.tavernquest.core.util.UuidProvider;import com.luminor.tavernquest.domain.model.*;import com.luminor.tavernquest.domain.repository.HeroRepository
class CreateHeroUseCase @Inject constructor(private val repo:HeroRepository,private val ids:UuidProvider,private val time:DateProvider){suspend operator fun invoke(name:String,heroClass:HeroClass,appearance:HeroAppearance)=repo.create(Hero(ids.newId(),name.trim(),appearance,heroClass,0,time.nowMillis()))}
