package com.kevin.receipttrackr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6B9BFF),
    secondary = Color(0xFF5FA4FF),
    tertiary = Color(0xFFFFB74D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    secondary = Color(0xFF42A5F5),
    tertiary = Color(0xFFFF9800),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
)

@Composable
fun ReceiptTrackrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
