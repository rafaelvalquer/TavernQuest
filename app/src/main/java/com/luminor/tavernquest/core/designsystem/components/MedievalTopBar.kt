package com.luminor.tavernquest.core.designsystem.components
import androidx.compose.material3.*; import androidx.compose.runtime.Composable
@OptIn(ExperimentalMaterial3Api::class) @Composable fun MedievalTopBar(title:String){ TopAppBar(title={Text(title)}) }
