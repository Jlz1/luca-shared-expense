package com.example.luca.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Skema warna untuk Mode Gelap (Dark Mode)
private val DarkColorScheme = darkColorScheme(
    primary = UIBlack,
    secondary = UIDarkGrey,
    tertiary = UIAccentYellow,
    background = UIBackground, // Opsional: Background utama jadi gelap
    surface = UIGrey,
    onPrimary = Color.White,
    onSurface = Color.White
)

// Skema warna untuk Mode Terang (Light Mode)
private val LightColorScheme = lightColorScheme(
    primary = UIBlack,
    secondary = UIDarkGrey,
    tertiary = UIAccentYellow,
    background = UIBackground, // Opsional: Background utama jadi gelap
    surface = UIGrey,
    onPrimary = Color.White,
    onSurface = Color.White
)

@Composable
fun LucaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // ... (logika colorScheme tetap sama)

    MaterialTheme(
        colorScheme = colorScheme,
        // Gunakan default Typography, kita akan pakai AppFont manual di UI
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}