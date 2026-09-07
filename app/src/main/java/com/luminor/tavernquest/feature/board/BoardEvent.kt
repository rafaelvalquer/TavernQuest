package com.luminor.tavernquest.feature.board
sealed interface BoardEvent{data class Accept(val id:String):BoardEvent}
