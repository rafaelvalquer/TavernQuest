package com.luminor.tavernquest.feature.quest.completion
import androidx.compose.material3.Text;import androidx.compose.runtime.Composable
@Composable fun ProofPhotoSection(path:String?){Text(if(path==null)"Foto de comprovação: opcional" else "Foto anexada")}
