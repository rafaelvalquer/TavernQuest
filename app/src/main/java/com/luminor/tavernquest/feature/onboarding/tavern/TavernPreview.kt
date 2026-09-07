package com.luminor.tavernquest.feature.onboarding.tavern
import androidx.compose.material3.Text;import androidx.compose.runtime.Composable;import com.luminor.tavernquest.domain.model.TavernEmblem
@Composable fun TavernPreview(name:String,e:TavernEmblem){Text("${e.title} • ${name.ifBlank{"Minha Taberna"}}")}
