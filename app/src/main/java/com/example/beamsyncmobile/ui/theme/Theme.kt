package com.example.beamsyncmobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BeamsyncColorScheme = darkColorScheme(
    primary = BeamsyncColors.accentPrimary,
    secondary = BeamsyncColors.accentSecondary,
    tertiary = BeamsyncColors.surfacePositive,
    background = BeamsyncColors.surfaceBase,
    surface = BeamsyncColors.surfaceRaised,
    surfaceVariant = BeamsyncColors.surfaceHigher,
    onPrimary = BeamsyncColors.surfaceBase,
    onSecondary = BeamsyncColors.surfaceBase,
    onTertiary = BeamsyncColors.surfaceBase,
    onBackground = BeamsyncColors.textPrimary,
    onSurface = BeamsyncColors.textPrimary,
    onSurfaceVariant = BeamsyncColors.textSecondary,
    outline = BeamsyncColors.strokeDefault,
    error = BeamsyncColors.surfaceCritical,
    onError = BeamsyncColors.surfaceBase,
)

@Composable
fun BeamSyncMobileTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BeamsyncColorScheme,
        typography = Typography,
        content = content,
    )
}
