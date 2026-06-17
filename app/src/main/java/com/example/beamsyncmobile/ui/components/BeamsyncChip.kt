package com.example.beamsyncmobile.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
    val (labelColor, containerColor) = when (variant) {
        BeamsyncChipVariant.Default -> Pair(
            MaterialTheme.colorScheme.onSurfaceVariant,
            Color.Transparent,
        )
        BeamsyncChipVariant.Active -> Pair(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
        )
        BeamsyncChipVariant.Success -> Pair(
            MaterialTheme.colorScheme.primary,
            Color.Transparent,
        )
        BeamsyncChipVariant.Error -> Pair(
            MaterialTheme.colorScheme.error,
            Color.Transparent,
        )
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = labelColor,
            containerColor = containerColor,
        ),
        shape = MaterialTheme.shapes.small,
    )
}
