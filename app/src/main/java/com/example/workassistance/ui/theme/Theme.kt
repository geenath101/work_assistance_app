package com.example.workassistance.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun WorkAssistanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Fixed, high-contrast palette (no dynamic colors) to keep the UI stable.
    val lightColors = lightColorScheme(
        primary = Color(0xFF1B6EF3),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD8E2FF),
        onPrimaryContainer = Color(0xFF001551),
        secondary = Color(0xFF005F73),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFB2EBF2),
        onSecondaryContainer = Color(0xFF001F26),
        surface = Color(0xFFF8F9FF),
        onSurface = Color(0xFF191C20),
        surfaceVariant = Color(0xFFE1E2EC),
        onSurfaceVariant = Color(0xFF44474F),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF)
    )

    val darkColors = darkColorScheme(
        primary = Color(0xFFADC6FF),
        onPrimary = Color(0xFF002E6D),
        primaryContainer = Color(0xFF00429A),
        onPrimaryContainer = Color(0xFFD8E2FF),
        secondary = Color(0xFF80D4E4),
        onSecondary = Color(0xFF003640),
        secondaryContainer = Color(0xFF004E5C),
        onSecondaryContainer = Color(0xFFB2EBF2),
        surface = Color(0xFF111318),
        onSurface = Color(0xFFE2E2E9),
        surfaceVariant = Color(0xFF44474F),
        onSurfaceVariant = Color(0xFFC4C6D0),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005)
    )

    val colorScheme = if (darkTheme) darkColors else lightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
