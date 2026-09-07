package com.luminor.tavernquest.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TavernColorScheme = darkColorScheme(
    primary = Gold,
    secondary = Ember,
    background = TavernBrown,
    surface = Wood,
    onPrimary = Ink,
    onBackground = Parchment,
    onSurface = Parchment,
)

@Composable
fun TavernQuestTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TavernColorScheme,
        typography = TavernTypography,
        shapes = TavernShapes,
        content = content,
    )
}
