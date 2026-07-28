package com.lysanderuy.tulogs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Dark-only for MVP — isSystemInDarkTheme() import is left wired for a future light variant
private val TuLogsColorScheme = darkColorScheme(
    background = Ink950,
    surface = Ink900,
    surfaceVariant = Ink800,
    outline = Ink700,
    onBackground = Paper50,
    onSurface = Paper50,
    primary = Amber500,
    onPrimary = Ink950,
    secondary = Periwinkle400,
    onSecondary = Ink950,
    error = Error500,
    onError = Paper50
)

@Composable
fun TuLogsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TuLogsColorScheme,
        content = content
    )
}
