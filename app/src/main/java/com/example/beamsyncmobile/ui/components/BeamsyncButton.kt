package com.example.beamsyncmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.ui.theme.BeamsyncColors

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

private val buttonSizes: Map<BeamsyncButtonSize, ButtonSizeValues> = mapOf(
    BeamsyncButtonSize.Small to ButtonSizeValues(32.dp, 12.dp, 14.sp, 16.dp),
    BeamsyncButtonSize.Default to ButtonSizeValues(48.dp, 16.dp, 16.sp, 20.dp),
    BeamsyncButtonSize.Large to ButtonSizeValues(56.dp, 24.dp, 20.sp, 24.dp),
)

private data class ButtonSizeValues(
    val height: Dp,
    val padding: Dp,
    val fontSize: androidx.compose.ui.unit.TextUnit,
    val iconSize: Dp,
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
    val (backgroundColor, textColor) = when (variant) {
        BeamsyncButtonVariant.Primary -> BeamsyncColors.accentPrimary to BeamsyncColors.surfaceBase
        BeamsyncButtonVariant.Secondary -> Color.Transparent to BeamsyncColors.accentPrimary
        BeamsyncButtonVariant.Destructive -> BeamsyncColors.surfaceCritical to BeamsyncColors.surfaceBase
        BeamsyncButtonVariant.Ghost -> Color.Transparent to BeamsyncColors.textSecondary
        BeamsyncButtonVariant.DataAction -> BeamsyncColors.surfaceBase to BeamsyncColors.accentSecondary
    }

    val useBorder = variant == BeamsyncButtonVariant.Secondary || variant == BeamsyncButtonVariant.DataAction
    val borderColor = when (variant) {
        BeamsyncButtonVariant.Secondary -> BeamsyncColors.accentPrimary
        BeamsyncButtonVariant.DataAction -> BeamsyncColors.strokeDefault
        else -> Color.Transparent
    }

    val sizeValues = buttonSizes[size]!!
    val disabledBg = if (enabled) backgroundColor else BeamsyncColors.surfaceHigher
    val disabledText = if (enabled) textColor else BeamsyncColors.textDisabled
    val finalBorder = if (enabled) borderColor else BeamsyncColors.strokeDefault
    val shape = RoundedCornerShape(0.dp)

    Box(
        modifier = Modifier
            .let { if (fullWidth) it.fillMaxWidth() else it }
            .height(sizeValues.height)
            .background(disabledBg, shape)
            .let { if (useBorder) it.border(2.dp, finalBorder, shape) else it }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = textColor.copy(alpha = 0.3f)),
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = sizeValues.padding),
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
                    tint = disabledText,
                    modifier = Modifier.size(sizeValues.iconSize),
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text.uppercase(),
                color = disabledText,
                fontSize = sizeValues.fontSize,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
            )
        }
    }
}
