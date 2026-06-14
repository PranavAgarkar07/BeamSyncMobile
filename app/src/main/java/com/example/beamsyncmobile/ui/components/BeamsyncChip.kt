package com.example.beamsyncmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.ui.theme.BeamsyncColors

enum class BeamsyncChipVariant {
    Default,
    Active,
    Success,
    Error,
}

@Composable
fun BeamsyncChip(
    text: String,
    variant: BeamsyncChipVariant = BeamsyncChipVariant.Default,
) {
    val (backgroundColor, textColor, borderColor) = when (variant) {
        BeamsyncChipVariant.Default -> Triple(
            Color.Transparent,
            BeamsyncColors.textSecondary,
            BeamsyncColors.strokeDefault,
        )
        BeamsyncChipVariant.Active -> Triple(
            Color.Transparent,
            BeamsyncColors.accentPrimary,
            BeamsyncColors.accentPrimary,
        )
        BeamsyncChipVariant.Success -> Triple(
            Color.Transparent,
            BeamsyncColors.surfacePositive,
            BeamsyncColors.surfacePositive,
        )
        BeamsyncChipVariant.Error -> Triple(
            Color.Transparent,
            BeamsyncColors.surfaceCritical,
            BeamsyncColors.surfaceCritical,
        )
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(0.dp))
            .border(1.dp, borderColor, RoundedCornerShape(0.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp,
        )
    }
}
