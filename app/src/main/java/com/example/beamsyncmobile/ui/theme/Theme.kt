package com.example.beamsyncmobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun BeamSyncMobileTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BeamsyncColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
