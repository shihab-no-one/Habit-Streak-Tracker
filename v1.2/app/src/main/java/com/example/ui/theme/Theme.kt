package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NordicDarkAccent,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = NordicDarkSecondaryAccent,
    onPrimaryContainer = NordicDarkTextPrimary,
    secondary = NordicDarkTextSecondary,
    onSecondary = Color(0xFF0F172A),
    background = NordicDarkBackground,
    onBackground = NordicDarkTextPrimary,
    surface = NordicDarkSurface,
    onSurface = NordicDarkTextPrimary,
    surfaceVariant = Color(0xFF243247),
    onSurfaceVariant = NordicDarkTextSecondary,
    outline = NordicDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = NordicLightAccent,
    onPrimary = Color.White,
    primaryContainer = NordicLightSecondaryAccent,
    onPrimaryContainer = NordicLightAccent,
    secondary = NordicLightTextSecondary,
    onSecondary = Color.White,
    background = NordicLightBackground,
    onBackground = NordicLightTextPrimary,
    surface = NordicLightSurface,
    onSurface = NordicLightTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = NordicLightTextSecondary,
    outline = NordicLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to Light Mode as requested
    dynamicColor: Boolean = false, // Use Nordic Sky curated palette
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

