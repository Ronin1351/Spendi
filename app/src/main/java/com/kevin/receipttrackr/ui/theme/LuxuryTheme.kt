package com.kevin.receipttrackr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkLuxuryColorScheme = darkColorScheme(
    primary = LuxuryGold,
    onPrimary = DeepPurple,
    primaryContainer = LuxuryGoldDark,
    onPrimaryContainer = WarmWhite,

    secondary = RoseGold,
    onSecondary = DeepPurple,
    secondaryContainer = Bronze,
    onSecondaryContainer = WarmWhite,

    tertiary = Champagne,
    onTertiary = DeepPurple,

    background = DeepPurple,
    onBackground = WarmWhite,

    surface = DarkPurple,
    onSurface = WarmWhite,
    surfaceVariant = MidPurple,
    onSurfaceVariant = LuxuryGoldLight,

    error = LuxuryError,
    onError = Color.White,

    outline = LuxuryGold.copy(alpha = 0.3f),
    outlineVariant = MidPurple,

    scrim = Color.Black.copy(alpha = 0.6f),
    inverseSurface = SoftCream,
    inverseOnSurface = DeepPurple,
    inversePrimary = LuxuryGoldDark,

    surfaceTint = LuxuryGold.copy(alpha = 0.1f),
)

private val LightLuxuryColorScheme = lightColorScheme(
    primary = LuxuryGoldDark,
    onPrimary = Color.White,
    primaryContainer = LuxuryGoldLight,
    onPrimaryContainer = DeepPurple,

    secondary = Bronze,
    onSecondary = Color.White,
    secondaryContainer = RoseGold,
    onSecondaryContainer = DeepPurple,

    tertiary = Champagne,
    onTertiary = DeepPurple,

    background = WarmWhite,
    onBackground = DeepPurple,

    surface = SoftCream,
    onSurface = DeepPurple,
    surfaceVariant = Champagne,
    onSurfaceVariant = DarkPurple,

    error = LuxuryError,
    onError = Color.White,

    outline = LuxuryGold.copy(alpha = 0.4f),
    outlineVariant = LuxuryGoldLight,

    scrim = Color.Black.copy(alpha = 0.4f),
    inverseSurface = DarkPurple,
    inverseOnSurface = WarmWhite,
    inversePrimary = LuxuryGold,

    surfaceTint = LuxuryGold.copy(alpha = 0.05f),
)

@Composable
fun LuxuryReceiptTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkLuxuryColorScheme
    } else {
        LightLuxuryColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LuxuryTypography,
        shapes = LuxuryShapes,
        content = content
    )
}
