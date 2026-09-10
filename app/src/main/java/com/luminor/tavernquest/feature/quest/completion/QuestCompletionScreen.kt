package com.luminor.tavernquest.feature.quest.completion
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luminor.tavernquest.core.designsystem.components.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable fun QuestCompletionScreen(vm:QuestCompletionViewModel=hiltViewModel(),onDone:()->Unit,onBack:()->Unit){
    val s by vm.ui.collectAsState()
    val context=androidx.compose.ui.platform.LocalContext.current
    val scope=rememberCoroutineScope()
    var showPhotoOptions by remember { mutableStateOf(false) }
    val gallery=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->if(uri!=null)scope.launch(Dispatchers.IO){vm.photo(copyPhoto(context,uri))}}
    Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
        Text("Concluir: ${s.title}");QuestNotesField(s.notes,vm::notes)
        ProofPhotoSection(s.photoPath)
        GoldOutlineButton("Adicionar foto",{showPhotoOptions=true},enabled=!s.saving)
        s.error?.let{Text(it)};GoldButton("Concluir missão",vm::finish,enabled=!s.saving);GoldOutlineButton("Cancelar",onBack)
    }
    if(showPhotoOptions) PhotoSourceDialog(onGallery={showPhotoOptions=false;gallery.launch("image/*")},onDismiss={showPhotoOptions=false})
    if(s.completed)QuestCompletedDialog(onDone)
}

private fun copyPhoto(context:Context,uri:Uri):String?=runCatching{
    val dir=File(context.filesDir,"quest_photos").apply{mkdirs()}
    val target=File(dir,"quest_${System.currentTimeMillis()}.jpg")
    val resolver=context.contentResolver
    val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true}
    resolver.openInputStream(uri).use { input -> BitmapFactory.decodeStream(input,null,bounds) }
    var sample=1
    while(bounds.outWidth/sample>1600||bounds.outHeight/sample>1600)sample*=2
    val bitmap=resolver.openInputStream(uri).use { input -> BitmapFactory.decodeStream(requireNotNull(input),null,BitmapFactory.Options().apply{inSampleSize=sample}) } ?: error("Não foi possível ler a foto.")
    var quality=88
    do {
        target.outputStream().use { output -> bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG,quality,output) }
        quality-=8
    } while(target.length()>2L*1024*1024&&quality>=48)
    bitmap.recycle()
    target.absolutePath
}.getOrNull()
