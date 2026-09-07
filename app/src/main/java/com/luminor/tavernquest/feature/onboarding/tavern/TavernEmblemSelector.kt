package com.luminor.tavernquest.feature.onboarding.tavern
import androidx.compose.foundation.layout.*;import androidx.compose.material3.*;import androidx.compose.runtime.Composable;import com.luminor.tavernquest.domain.model.TavernEmblem
@Composable fun TavernEmblemSelector(v:TavernEmblem,onSelect:(TavernEmblem)->Unit){Column{Text("Brasão");TavernEmblem.entries.forEach{FilterChip(v==it,{onSelect(it)},{Text(it.title)})}}}
