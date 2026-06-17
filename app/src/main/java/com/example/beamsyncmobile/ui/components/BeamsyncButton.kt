package com.example.beamsyncmobile.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

enum class BeamsyncButtonVariant {
    Primary,
    Secondary,
    Destructive,
    Ghost,
    DataAction,
}

enum class BeamsyncButtonSize {
    Small,
    Default,
    Large,
}

private data class ButtonSizeValues(
    val height: Dp,
    val hPadding: Dp,
    val fontSize: TextUnit,
    val iconSize: Dp,
)

private val buttonSizes = mapOf(
    BeamsyncButtonSize.Small   to ButtonSizeValues(32.dp, 12.dp, 13.sp, 16.dp),
    BeamsyncButtonSize.Default to ButtonSizeValues(48.dp, 16.dp, 15.sp, 20.dp),
    BeamsyncButtonSize.Large   to ButtonSizeValues(56.dp, 24.dp, 16.sp, 22.dp),
)

@Composable
fun BeamsyncButton(
    text: String,
    variant: BeamsyncButtonVariant = BeamsyncButtonVariant.Primary,
    size: BeamsyncButtonSize = BeamsyncButtonSize.Default,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = tween(durationMillis = 80), // Matches press duration
        label = "btn-scale",
    )

    val sz = buttonSizes[size]!!
    val shape = MaterialTheme.shapes.small

    val backgroundColor: Color
    val textColor: Color
    val borderColor: Color?

    when (variant) {
        BeamsyncButtonVariant.Primary -> {
            backgroundColor = if (enabled) MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.surfaceVariant
            textColor = if (enabled) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            borderColor = null
        }

        BeamsyncButtonVariant.Secondary -> {
            backgroundColor = Color.Transparent
            textColor = if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            borderColor = if (enabled) MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.outline
        }

        BeamsyncButtonVariant.Destructive -> {
            backgroundColor = if (enabled) MaterialTheme.colorScheme.error
                              else MaterialTheme.colorScheme.surfaceVariant
            textColor = if (enabled) MaterialTheme.colorScheme.onError
                        else MaterialTheme.colorScheme.onSurfaceVariant
            borderColor = null
        }

        BeamsyncButtonVariant.Ghost -> {
            backgroundColor = Color.Transparent
            textColor = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant
            borderColor = null
        }

        BeamsyncButtonVariant.DataAction -> {
            backgroundColor = MaterialTheme.colorScheme.surface
            textColor = if (enabled) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            borderColor = MaterialTheme.colorScheme.outline
        }
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .let { if (fullWidth) it.fillMaxWidth() else it }
            .height(sz.height)
            .background(backgroundColor, shape)
            .let {
                if (borderColor != null) it.border(1.5.dp, borderColor, shape) else it
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = textColor.copy(alpha = 0.25f),
                ),
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = sz.hPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(sz.iconSize),
                )
                Spacer(Modifier.width(BeamsyncSpacing.space1))
            }
            Text(
                text = text.uppercase(),
                color = textColor,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
