package com.luminor.tavernquest.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.luminor.tavernquest.core.designsystem.theme.TavernBrown

@Composable
fun ParchmentBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.background(TavernBrown)) {
        content()
    }
}
