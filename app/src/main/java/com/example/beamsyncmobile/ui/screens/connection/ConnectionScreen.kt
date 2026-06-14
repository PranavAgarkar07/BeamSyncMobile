package com.example.beamsyncmobile.ui.screens.connection

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beamsyncmobile.network.ServerConnection
import com.example.beamsyncmobile.ui.components.BeamsyncButton
import com.example.beamsyncmobile.ui.components.BeamsyncButtonSize
import com.example.beamsyncmobile.ui.components.BeamsyncButtonVariant
import com.example.beamsyncmobile.ui.theme.BeamsyncColors
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

@Composable
fun ConnectionScreen(
    navController: NavController,
    connection: ServerConnection,
) {
    var heartbeatState by remember { mutableStateOf("Connecting...") }
    var isAlive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeamsyncColors.surfaceBase)
            .padding(BeamsyncSpacing.space8)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "CONNECTED",
            color = BeamsyncColors.surfacePositive,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        // Connection details card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BeamsyncColors.surfaceRaised)
                .border(1.dp, BeamsyncColors.strokeDefault, RoundedCornerShape(0.dp))
                .padding(BeamsyncSpacing.space4),
        ) {
            Column {
                DetailRow("HOST", connection.host)
                DetailRow("PORT", connection.port.toString())
                DetailRow("SCHEME", connection.scheme.uppercase())
                DetailRow("TOKEN", connection.token.take(16) + "...")
                DetailRow("STATUS", if (isAlive) "ONLINE" else "CONNECTING...")
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        // Transfer actions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BeamsyncColors.surfaceRaised)
                .border(1.dp, BeamsyncColors.strokeDefault, RoundedCornerShape(0.dp))
                .padding(BeamsyncSpacing.space4),
        ) {
            Column {
                Text(
                    text = "READY FOR TRANSFER",
                    color = BeamsyncColors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(BeamsyncSpacing.space4))
                Text(
                    text = "BeamSync Desktop is ready. Use the tabs below to send or receive files.",
                    color = BeamsyncColors.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        // Action buttons
        BeamsyncButton(
            text = "RECEIVE FILES FROM DESKTOP",
            variant = BeamsyncButtonVariant.Primary,
            size = BeamsyncButtonSize.Large,
            fullWidth = true,
            onClick = {
                navController.navigate("downloads") {
                    popUpTo("scan") { inclusive = false }
                }
            },
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        BeamsyncButton(
            text = "SEND FILES TO DESKTOP",
            variant = BeamsyncButtonVariant.Secondary,
            size = BeamsyncButtonSize.Large,
            fullWidth = true,
            onClick = {
                navController.navigate("uploads") {
                    popUpTo("scan") { inclusive = false }
                }
            },
        )

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        // Disconnect
        BeamsyncButton(
            text = "DISCONNECT",
            variant = BeamsyncButtonVariant.Ghost,
            size = BeamsyncButtonSize.Default,
            fullWidth = true,
            onClick = {
                navController.popBackStack("scan", inclusive = false)
            },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = BeamsyncColors.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
        )
        Text(
            text = value,
            color = BeamsyncColors.textPrimary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}
