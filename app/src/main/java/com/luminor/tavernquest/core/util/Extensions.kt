package com.luminor.tavernquest.core.util
fun String.nonBlankOr(default:String)=if(isBlank()) default else trim()
