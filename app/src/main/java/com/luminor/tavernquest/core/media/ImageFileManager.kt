package com.luminor.tavernquest.core.media
import android.content.Context;import java.io.File
class ImageFileManager(private val context:Context):PhotoManager{override fun createPhotoPath():String{val d=File(context.filesDir,"quest_photos").apply{mkdirs()};return File(d,"quest_${System.currentTimeMillis()}.jpg").absolutePath}}
