package com.luminor.tavernquest.domain.usecase.quest
import javax.inject.Inject
import com.luminor.tavernquest.domain.repository.*
class CompleteQuestUseCase @Inject constructor(private val quests:QuestRepository,private val heroes:HeroRepository){suspend operator fun invoke(id:String,notes:String,photo:String?):Boolean{val h=heroes.get()?:return false;return quests.complete(id,h.id,notes,photo)}}
