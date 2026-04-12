package com.flipwise.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary      = GrapePop,
    secondary    = CoralZest,
    tertiary     = MintGreen,
    background   = GhostWhite,
    surface      = Color.White,
    onPrimary    = Color.White,
    onSecondary  = Color.White,
    onBackground = NavyInk,
    onSurface    = NavyInk,
    error        = CherryRed
)

@Composable
fun FlipWiseTheme(content: @Composable () -> Unit) {
    ProvideResponsiveDimensions {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography  = Typography,
            content     = content
        )
    }
}