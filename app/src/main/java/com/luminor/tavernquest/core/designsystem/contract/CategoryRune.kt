package com.luminor.tavernquest.core.designsystem.contract
import androidx.compose.material3.Text;import androidx.compose.runtime.Composable;import com.luminor.tavernquest.domain.model.*
@Composable fun CategoryRune(c:ContractCategory){Text(c.title.take(1))}
