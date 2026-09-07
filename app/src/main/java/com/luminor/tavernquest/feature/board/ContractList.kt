package com.luminor.tavernquest.feature.board
import androidx.compose.foundation.layout.*;import androidx.compose.foundation.lazy.*;import androidx.compose.runtime.Composable;import androidx.compose.ui.Modifier;import androidx.compose.ui.unit.dp;import com.luminor.tavernquest.core.designsystem.contract.ContractCard;import com.luminor.tavernquest.domain.model.DailyContract
@Composable fun ContractList(items:List<DailyContract>,onQuest:(String)->Unit){LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){items(items,key={it.id}){ContractCard(it,{onQuest(it.id)},Modifier.fillMaxWidth())}}}
