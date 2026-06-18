package com.example.beamsyncmobile.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

@Composable
fun ScannerOverlay(
    hintText: String,
    torchEnabled: Boolean,
    onToggleTorch: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()

    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val bracketGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)),
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.Center),
        ) {
            Bracket(Alignment.TopStart, 36.dp, 3.dp, primary, bracketGlow)
            Bracket(Alignment.TopEnd, 36.dp, 3.dp, primary, bracketGlow)
            Bracket(Alignment.BottomStart, 36.dp, 3.dp, primary, bracketGlow)
            Bracket(Alignment.BottomEnd, 36.dp, 3.dp, primary, bracketGlow)

            val scanOffset = (280.dp - 4.dp) * scanLineProgress
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.TopStart)
                    .offset(y = scanOffset)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                primary.copy(alpha = 0.2f),
                                primary.copy(alpha = 0.6f),
                                primary,
                                primary.copy(alpha = 0.6f),
                                primary.copy(alpha = 0.2f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .align(Alignment.TopStart)
                    .offset(y = scanOffset + 4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                primary.copy(alpha = 0.05f),
                                primary.copy(alpha = 0.15f),
                                primary.copy(alpha = 0.05f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }

        Text(
            text = hintText,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 180.dp),
            textAlign = TextAlign.Center,
        )

        Text(
            text = "SCANNING...",
            color = primary.copy(alpha = pulseAlpha),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            textAlign = TextAlign.Center,
        )

        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(BeamsyncSpacing.space4),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel scanning",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Cancel",
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        IconButton(
            onClick = onToggleTorch,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(BeamsyncSpacing.space6)
                .size(48.dp)
                .background(
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    MaterialTheme.shapes.extraLarge,
                ),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = if (torchEnabled) "Disable flash" else "Enable flash",
                tint = if (torchEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
fun BoxScope.Bracket(
    alignment: Alignment,
    length: Dp = 36.dp,
    thickness: Dp = 3.dp,
    color: Color,
    glowAlpha: Float = 1f,
) {
    Box(
        modifier = Modifier
            .size(length + 2.dp)
            .align(alignment),
    ) {
        val isHorizontal = alignment == Alignment.TopStart || alignment == Alignment.TopEnd
        Box(
            modifier = Modifier
                .align(alignment)
                .let {
                    if (isHorizontal) it.width(length).height(thickness)
                    else it.width(thickness).height(length)
                }
                .background(color.copy(alpha = glowAlpha), RoundedCornerShape(thickness / 2)),
        )
        Box(
            modifier = Modifier
                .align(alignment)
                .let {
                    if (isHorizontal) it.width(length / 3).height(thickness * 3)
                    else it.width(thickness * 3).height(length / 3)
                }
                .offset(
                    x = if (!isHorizontal && alignment == Alignment.TopStart) -(thickness) else if (!isHorizontal) 0.dp else 0.dp,
                    y = if (isHorizontal && alignment == Alignment.TopStart) -(thickness) else if (isHorizontal && alignment == Alignment.TopEnd) 0.dp else 0.dp,
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = glowAlpha * 0.4f),
                            color.copy(alpha = glowAlpha * 0.1f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
fun ConnectingOverlay(
    label: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(BeamsyncSpacing.space4))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(BeamsyncSpacing.space6))
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                )
            }
        }
    }
}
