package com.luminor.tavernquest.feature.quest.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.luminor.tavernquest.core.designsystem.components.GoldButton
import com.luminor.tavernquest.core.designsystem.components.GoldOutlineButton
import com.luminor.tavernquest.domain.model.ContractStatus

@Composable
fun QuestActions(
    status: ContractStatus,
    onAccept: () -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when (status) {
            ContractStatus.AVAILABLE -> GoldButton("Aceitar", onAccept)
            ContractStatus.ACCEPTED -> GoldButton("Concluir", onComplete)
            else -> Unit
        }
        GoldOutlineButton("Voltar", onBack)
    }
}
