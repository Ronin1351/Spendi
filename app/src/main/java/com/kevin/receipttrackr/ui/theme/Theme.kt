package com.kevin.receipttrackr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
fun ReceiptTrackrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    LuxuryReceiptTheme(
        darkTheme = darkTheme,
        content = content
    )
}
