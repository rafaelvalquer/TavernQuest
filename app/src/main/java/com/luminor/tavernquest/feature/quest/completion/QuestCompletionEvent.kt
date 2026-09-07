package com.luminor.tavernquest.feature.quest.completion
sealed interface QuestCompletionEvent{data class Notes(val value:String):QuestCompletionEvent;data object Complete:QuestCompletionEvent}
