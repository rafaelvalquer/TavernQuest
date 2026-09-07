package com.luminor.tavernquest.feature.tavern
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*;import androidx.compose.material3.Text;import androidx.compose.runtime.Composable;import com.luminor.tavernquest.core.designsystem.contract.ContractCard;import com.luminor.tavernquest.domain.model.DailyContract
@Composable fun ActiveContractsSection(items:List<DailyContract>,onQuest:(String)->Unit){Column{Text("Contratos ativos");if(items.isEmpty())Text("Aceite missões no mural.");items.forEach{ContractCard(it,{onQuest(it.id)},Modifier.fillMaxWidth())}}}
