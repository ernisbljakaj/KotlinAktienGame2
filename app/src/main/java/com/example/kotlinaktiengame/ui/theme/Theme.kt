package com.example.kotlinaktiengame.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Typography

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF0D47A1),
    secondary = androidx.compose.ui.graphics.Color(0xFF1976D2),
    tertiary = androidx.compose.ui.graphics.Color(0xFF00ACC1)
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF90CAF9),
    secondary = androidx.compose.ui.graphics.Color(0xFF42A5F5)
)

@Composable
fun KotlinAktienGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
