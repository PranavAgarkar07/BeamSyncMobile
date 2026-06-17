package com.example.beamsyncmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

@Composable
fun BeamsyncCard(
    modifier: Modifier = Modifier,
    accentBarColor: Color? = null,
    accentBarGradient: Brush? = null,
    header: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val bar: Brush? = accentBarGradient
        ?: accentBarColor?.let { Brush.horizontalGradient(listOf(it, it.copy(alpha = 0.4f), Color.Transparent)) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        if (bar != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(bar),
            )
        }

        Column(modifier = Modifier.padding(BeamsyncSpacing.space4)) {
            if (header != null) {
                header()
                Spacer(Modifier.height(BeamsyncSpacing.space3))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline),
                )
                Spacer(Modifier.height(BeamsyncSpacing.space3))
            }
            content()
        }
    }
}

enum class TransferStatus {
    Queued,
    Transferring,
    Completed,
    Failed,
}

@Composable
fun TransferCard(
    fileName: String,
    fileSize: String,
    progress: Float,
    speed: String? = null,
    eta: String? = null,
    status: TransferStatus,
    onCancel: (() -> Unit)? = null,
) {
    val statusColor: Color
    val statusText: String
    val accentBrush: Brush?

    when (status) {
        TransferStatus.Queued -> {
            statusColor = MaterialTheme.colorScheme.onSurfaceVariant
            statusText = "QUEUED"
            accentBrush = null
        }
        TransferStatus.Transferring -> {
            statusColor = MaterialTheme.colorScheme.primary
            statusText = "${(progress * 100).toInt()}%"
            accentBrush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    Color.Transparent,
                ),
            )
        }
        TransferStatus.Completed -> {
            statusColor = MaterialTheme.colorScheme.primary
            statusText = "TRANSFERRED"
            accentBrush = Brush.horizontalGradient(
                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), Color.Transparent),
            )
        }
        TransferStatus.Failed -> {
            statusColor = MaterialTheme.colorScheme.error
            statusText = "FAILED"
            accentBrush = Brush.horizontalGradient(
                colors = listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), Color.Transparent),
            )
        }
    }

    BeamsyncCard(accentBarGradient = accentBrush) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileName,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fileSize,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (speed != null && status == TransferStatus.Transferring) {
                            Text(
                                text = "  ↑  $speed",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    if (eta != null && status == TransferStatus.Transferring) {
                        Text(
                            text = "ETA $eta",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            if (status == TransferStatus.Transferring || status == TransferStatus.Queued) {
                Spacer(Modifier.height(BeamsyncSpacing.space3))
                BeamsyncProgressBar(
                    progress = progress,
                    fillColor = statusColor,
                )
            }
        }
    }
}
