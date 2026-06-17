package com.example.beamsyncmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

@Composable
fun TelemetryCard(
    uploadSpeed: String = "0 B/s",
    downloadSpeed: String = "0 B/s",
    uploadedBytes: String = "0 B",
    downloadedBytes: String = "0 B",
    eta: String = "--",
    ipAddress: String = "---.---.---.---",
    connectionStatus: String = "DISCONNECTED",
    wifiBand: String = "--",
) {
    val statusColor = when (connectionStatus) {
        "CONNECTED" -> MaterialTheme.colorScheme.primary
        "CONNECTING" -> MaterialTheme.colorScheme.primary
        "DISCONNECTED" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.error
    }

    BeamsyncCard(accentBarColor = MaterialTheme.colorScheme.secondary) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            DataValue(
                value = uploadSpeed,
                label = "UPLOAD",
                valueColor = MaterialTheme.colorScheme.secondary,
            )
            DataValue(
                value = downloadSpeed,
                label = "DOWNLOAD",
                valueColor = MaterialTheme.colorScheme.secondary,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline),
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            DataValue(
                value = uploadedBytes,
                label = "\u2191 SENT",
                valueColor = MaterialTheme.colorScheme.onSurface,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DataValue(
                value = downloadedBytes,
                label = "\u2193 RECEIVED",
                valueColor = MaterialTheme.colorScheme.onSurface,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DataValue(
                value = eta,
                label = "ETA",
                valueColor = MaterialTheme.colorScheme.onSurface,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline),
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(statusColor, MaterialTheme.shapes.small)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = connectionStatus,
                    color = MaterialTheme.colorScheme.surface,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = ipAddress,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
            )
            Spacer(Modifier.width(BeamsyncSpacing.space2))
            Text(
                text = wifiBand,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }
    }
}
