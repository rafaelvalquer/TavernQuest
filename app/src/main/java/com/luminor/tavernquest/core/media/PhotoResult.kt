package com.luminor.tavernquest.core.media
sealed interface PhotoResult{data class Success(val path:String):PhotoResult;data class Error(val reason:String):PhotoResult;data object Cancelled:PhotoResult}
