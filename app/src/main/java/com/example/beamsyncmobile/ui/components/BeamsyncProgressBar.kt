package com.example.beamsyncmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.beamsyncmobile.ui.theme.BeamsyncColors
import com.example.beamsyncmobile.ui.theme.BeamsyncAnimationSpec
import androidx.compose.animation.core.animateFloatAsState

@Composable
fun BeamsyncProgressBar(
    progress: Float,
    fillColor: Color = BeamsyncColors.accentPrimary,
    trackColor: Color = BeamsyncColors.surfaceHigher,
    height: androidx.compose.ui.unit.Dp = 4.dp,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = BeamsyncAnimationSpec.progressStep(),
        label = "progress",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(trackColor, RoundedCornerShape(0.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxWidth()
                .height(height)
                .background(fillColor, RoundedCornerShape(0.dp)),
        )
    }
}
