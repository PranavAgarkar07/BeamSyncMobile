package com.example.beamsyncmobile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.ui.theme.BeamsyncAnimationSpec
import com.example.beamsyncmobile.ui.theme.BeamsyncColors
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

@Composable
fun BeamsyncDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String = "CONFIRM",
    dismissText: String = "CANCEL",
    destructive: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val dialogScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = BeamsyncAnimationSpec.dialogScale(),
        label = "dialogScale",
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = BeamsyncAnimationSpec.scrimFade()),
        exit = fadeOut(animationSpec = BeamsyncAnimationSpec.scrimFade()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BeamsyncColors.overlayDim),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = BeamsyncSpacing.space8)
                    .scale(dialogScale)
                    .background(BeamsyncColors.surfaceRaised, RoundedCornerShape(0.dp))
                    .border(
                        1.dp,
                        if (destructive) BeamsyncColors.surfaceCritical else BeamsyncColors.accentPrimary,
                        RoundedCornerShape(0.dp),
                    )
                    .padding(BeamsyncSpacing.space6),
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                if (destructive) BeamsyncColors.surfaceCritical
                                else BeamsyncColors.accentPrimary,
                            ),
                    )
                    Spacer(Modifier.height(BeamsyncSpacing.space4))

                    Text(
                        text = title,
                        color = BeamsyncColors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(BeamsyncSpacing.space3))

                    Text(
                        text = message,
                        color = BeamsyncColors.textSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    )
                    Spacer(Modifier.height(BeamsyncSpacing.space6))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        BeamsyncButton(
                            text = dismissText,
                            variant = BeamsyncButtonVariant.Ghost,
                            size = BeamsyncButtonSize.Small,
                            onClick = onDismiss,
                        )
                        Spacer(Modifier.width(BeamsyncSpacing.space2))
                        BeamsyncButton(
                            text = confirmText,
                            variant = if (destructive) BeamsyncButtonVariant.Destructive
                                     else BeamsyncButtonVariant.Primary,
                            size = BeamsyncButtonSize.Small,
                            onClick = onConfirm,
                        )
                    }
                }
            }
        }
    }
}
