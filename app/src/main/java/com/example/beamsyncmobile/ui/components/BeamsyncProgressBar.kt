package com.example.beamsyncmobile.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.beamsyncmobile.ui.theme.BeamsyncColors

@Composable
fun BeamsyncProgressBar(
    progress: Float,
    fillColor: Color = BeamsyncColors.accentPrimary,
    trackColor: Color = BeamsyncColors.surfaceHigher,
    height: Dp = 4.dp,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = snap(),
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

@Composable
fun BeamsyncCircularProgress(
    progress: Float,
    size: Dp = 160.dp,
    strokeWidth: Dp = 8.dp,
    fillColor: Color = BeamsyncColors.accentPrimary,
    trackColor: Color = BeamsyncColors.surfaceHigher,
    content: @Composable (() -> Unit)? = null,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = snap(),
        label = "circularProgress",
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            val arcSize = Size(size.toPx() - strokeWidth.toPx(), size.toPx() - strokeWidth.toPx())
            val topLeft = Offset(strokeWidth.toPx() / 2f, strokeWidth.toPx() / 2f)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )

            drawArc(
                color = fillColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }

        if (content != null) {
            Box(contentAlignment = Alignment.Center) {
                content()
            }
        }
    }
}
