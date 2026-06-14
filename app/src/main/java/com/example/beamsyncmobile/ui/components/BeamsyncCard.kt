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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.ui.theme.BeamsyncColors
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

// Optional leading accent bar color (e.g. #00E5FF for data cards)
@Composable
fun BeamsyncCard(
    modifier: Modifier = Modifier,
    accentBarColor: Color? = null,
    header: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BeamsyncColors.surfaceRaised, RoundedCornerShape(0.dp))
            .border(1.dp, BeamsyncColors.strokeDefault, RoundedCornerShape(0.dp))
            .padding(BeamsyncSpacing.space4),
    ) {
        if (accentBarColor != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(accentBarColor),
            )
            Spacer(Modifier.height(BeamsyncSpacing.space3))
        }
        if (header != null) {
            header()
            Spacer(Modifier.height(BeamsyncSpacing.space3))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BeamsyncColors.strokeDefault),
            )
            Spacer(Modifier.height(BeamsyncSpacing.space3))
        }
        content()
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
    val icon: String
    val statusColor: Color
    val statusText: String

    when (status) {
        TransferStatus.Queued -> {
            icon = "\u23F3"
            statusColor = BeamsyncColors.textSecondary
            statusText = "QUEUED"
        }
        TransferStatus.Transferring -> {
            icon = "\u25D4"
            statusColor = BeamsyncColors.accentPrimary
            statusText = "${(progress * 100).toInt()}%"
        }
        TransferStatus.Completed -> {
            icon = "\u2713"
            statusColor = BeamsyncColors.surfacePositive
            statusText = "TRANSFERRED"
        }
        TransferStatus.Failed -> {
            icon = "\u2717"
            statusColor = BeamsyncColors.surfaceCritical
            statusText = "FAILED"
        }
    }

    BeamsyncCard(
        accentBarColor = when (status) {
            TransferStatus.Completed -> BeamsyncColors.surfacePositive
            TransferStatus.Transferring -> BeamsyncColors.accentPrimary
            TransferStatus.Failed -> BeamsyncColors.surfaceCritical
            TransferStatus.Queued -> null
        }
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // File info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileName,
                        color = BeamsyncColors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fileSize,
                            color = BeamsyncColors.textSecondary,
                            fontSize = 12.sp,
                        )
                        if (speed != null && status == TransferStatus.Transferring) {
                            Text(
                                text = "  \u2B50  $speed",
                                color = BeamsyncColors.accentSecondary,
                                fontSize = 12.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            )
                        }
                    }
                }

                // Status + action
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                    if (eta != null && status == TransferStatus.Transferring) {
                        Text(
                            text = "ETA $eta",
                            color = BeamsyncColors.textSecondary,
                            fontSize = 11.sp,
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
