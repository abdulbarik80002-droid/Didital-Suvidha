package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF002244),
    primaryContainer = Color(0xFF134074),
    onPrimaryContainer = Color(0xFFD4E5FF),
    secondary = DarkSecondary,
    onSecondary = Color(0xFF452200),
    secondaryContainer = Color(0xFF6B3A00),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = Color(0xFF68D8D6),
    background = DarkBg,
    onBackground = Color(0xFFE2E8F0),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334E68)
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E7F8),
    onPrimaryContainer = Color(0xFF06182B),
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE7CC),
    onSecondaryContainer = Color(0xFF4A2500),
    tertiary = SuvidhaTeal,
    background = LightBg,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun DigitalSuvidhaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
