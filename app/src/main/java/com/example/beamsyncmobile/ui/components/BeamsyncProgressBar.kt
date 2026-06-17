package com.example.beamsyncmobile.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.beamsyncmobile.ui.theme.BeamsyncColors

@Composable
fun BeamsyncProgressBar(
    progress: Float,
    fillColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 4.dp,
    useGradient: Boolean = true,
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
            .background(trackColor, MaterialTheme.shapes.small),
    ) {
        // TODO: Replace with M3 tonal palette when available
        val fill: Brush = if (useGradient) {
            Brush.horizontalGradient(
                colors = listOf(
                    BeamsyncColors.accentGradientStart,
                    BeamsyncColors.accentGradientMid,
                    BeamsyncColors.accentGradientEnd,
                ),
            )
        } else {
            Brush.horizontalGradient(listOf(fillColor, fillColor))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(height)
                .background(fill, MaterialTheme.shapes.small),
        )
    }
}

@Composable
fun BeamsyncCircularProgress(
    progress: Float,
    size: Dp = 160.dp,
    strokeWidth: Dp = 8.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
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
            val arcSizePx = size.toPx() - strokeWidth.toPx()
            val arcSize = Size(arcSizePx, arcSizePx)
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

            val sweepColor = lerp(
                BeamsyncColors.accentGradientStart,
                BeamsyncColors.accentGradientEnd,
                animatedProgress,
            )
            drawArc(
                color = sweepColor,
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

private fun lerp(a: Color, b: Color, t: Float): Color = Color(
    red   = a.red   + (b.red   - a.red)   * t,
    green = a.green + (b.green - a.green) * t,
    blue  = a.blue  + (b.blue  - a.blue)  * t,
    alpha = a.alpha + (b.alpha - a.alpha) * t,
)
