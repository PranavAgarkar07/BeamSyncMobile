package com.example.beamsyncmobile.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

// Spacing system based on 4px base unit
// All values are multiples of 4px for perfect vertical rhythm

object BeamsyncSpacing {
    val space0: androidx.compose.ui.unit.Dp = 0.dp
    val space1: androidx.compose.ui.unit.Dp = 4.dp
    val space2: androidx.compose.ui.unit.Dp = 8.dp
    val space3: androidx.compose.ui.unit.Dp = 12.dp
    val space4: androidx.compose.ui.unit.Dp = 16.dp
    val space5: androidx.compose.ui.unit.Dp = 20.dp
    val space6: androidx.compose.ui.unit.Dp = 24.dp
    val space8: androidx.compose.ui.unit.Dp = 32.dp
    val space10: androidx.compose.ui.unit.Dp = 40.dp
    val space12: androidx.compose.ui.unit.Dp = 48.dp
    val space16: androidx.compose.ui.unit.Dp = 64.dp
    val space20: androidx.compose.ui.unit.Dp = 80.dp

    val minTouchTarget: androidx.compose.ui.unit.Dp = 48.dp
    val preferredTouchTarget: androidx.compose.ui.unit.Dp = 56.dp

    val iconSmall: androidx.compose.ui.unit.Dp = 16.dp
    val iconDefault: androidx.compose.ui.unit.Dp = 20.dp
    val iconLarge: androidx.compose.ui.unit.Dp = 24.dp

    val progressBarHeight: androidx.compose.ui.unit.Dp = 4.dp

    val qrCornerSize: androidx.compose.ui.unit.Dp = 12.dp
    val qrScrimAlpha: Float = 0.7f

    val screenHorizontalMargin: androidx.compose.ui.unit.Dp = space8
    val screenVerticalMargin: androidx.compose.ui.unit.Dp = space12
}

object BeamsyncEdgeInsets {
    val screen: PaddingValues = PaddingValues(
        start = BeamsyncSpacing.screenHorizontalMargin,
        end = BeamsyncSpacing.screenHorizontalMargin,
        top = BeamsyncSpacing.screenVerticalMargin,
        bottom = BeamsyncSpacing.screenVerticalMargin,
    )

    val content: PaddingValues = PaddingValues(
        start = BeamsyncSpacing.space8,
        end = BeamsyncSpacing.space8,
        top = BeamsyncSpacing.space6,
        bottom = BeamsyncSpacing.space6,
    )

    val list: PaddingValues = PaddingValues(
        start = BeamsyncSpacing.space0,
        end = BeamsyncSpacing.space0,
        top = BeamsyncSpacing.space0,
        bottom = BeamsyncSpacing.space6,
    )
}
