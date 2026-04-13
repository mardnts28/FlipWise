package com.flipwise.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AppDimensions(
    val logoSize: Dp,
    val paddingSmall: Dp,
    val paddingMedium: Dp,
    val paddingLarge: Dp,
    val titleFontSize: TextUnit,
    val headerFontSize: TextUnit,
    val bodyFontSize: TextUnit,
    val detailFontSize: TextUnit,
    val buttonHeight: Dp,
    val cardCornerRadius: Dp,
    val isTablet: Boolean = false
)

val LocalAppDimensions = staticCompositionLocalOf {
    phoneDimensions // Default
}

val phoneDimensions = AppDimensions(
    logoSize = 140.dp,
    paddingSmall = 8.dp,
    paddingMedium = 16.dp,
    paddingLarge = 24.dp,
    titleFontSize = 42.sp,
    headerFontSize = 24.sp,
    bodyFontSize = 18.sp,
    detailFontSize = 14.sp,
    buttonHeight = 64.dp,
    cardCornerRadius = 32.dp,
    isTablet = false
)

val tabletDimensions = AppDimensions(
    logoSize = 240.dp,
    paddingSmall = 12.dp,
    paddingMedium = 24.dp,
    paddingLarge = 48.dp,
    titleFontSize = 64.sp,
    headerFontSize = 36.sp,
    bodyFontSize = 24.sp,
    detailFontSize = 18.sp,
    buttonHeight = 72.dp,
    cardCornerRadius = 40.dp,
    isTablet = true
)

@Composable
fun ProvideResponsiveDimensions(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    val dimensions = if (screenWidth >= 600) {
        tabletDimensions
    } else {
        phoneDimensions
    }

    CompositionLocalProvider(LocalAppDimensions provides dimensions) {
        content()
    }
}

object FlipWiseDesign {
    val dimensions: AppDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalAppDimensions.current
}
