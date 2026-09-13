package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = OnPrimaryCyan,
    primaryContainer = PrimaryContainerCyan,
    onPrimaryContainer = OnPrimaryContainerCyan,
    secondary = SecondaryViolet,
    onSecondary = OnSecondaryViolet,
    secondaryContainer = SecondaryContainerViolet,
    onSecondaryContainer = OnSecondaryContainerViolet,
    tertiary = TertiaryEmerald,
    onTertiary = OnTertiaryEmerald,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    outline = DarkOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek obsidian theme for AI Assistant
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
