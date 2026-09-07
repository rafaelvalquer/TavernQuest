package com.luminor.tavernquest.feature.board
import androidx.compose.ui.Modifier
import androidx.compose.foundation.horizontalScroll;import androidx.compose.foundation.layout.Row;import androidx.compose.foundation.rememberScrollState;import androidx.compose.material3.*;import androidx.compose.runtime.Composable;import com.luminor.tavernquest.domain.model.ContractCategory
@Composable fun ContractCategoryBar(selected:ContractCategory?,onSelect:(ContractCategory?)->Unit){Row(Modifier.horizontalScroll(rememberScrollState())){FilterChip(selected==null,{onSelect(null)},{Text("Todos")});ContractCategory.entries.forEach{FilterChip(selected==it,{onSelect(it)},{Text(it.title)})}}}
