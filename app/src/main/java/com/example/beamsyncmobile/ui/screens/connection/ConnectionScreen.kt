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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beamsyncmobile.R
import com.example.beamsyncmobile.network.ServerConnection
import com.example.beamsyncmobile.ui.components.BeamsyncButton
import com.example.beamsyncmobile.ui.components.BeamsyncButtonSize
import com.example.beamsyncmobile.ui.components.BeamsyncButtonVariant
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

@Composable
fun ConnectionScreen(
    navController: NavController,
    connection: ServerConnection,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(BeamsyncSpacing.space8)
            .verticalScroll(rememberScrollState()),
    ) {
        val glowColor = MaterialTheme.colorScheme.primaryContainer
        Box(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.CenterHorizontally)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor,
                                Color.Transparent,
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.width * 0.9f,
                        ),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "BeamSync",
                tint = Color.Unspecified,
                modifier = Modifier.size(56.dp),
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space4))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
            )
            Spacer(Modifier.size(BeamsyncSpacing.space2))
            Text(
                text = "CONNECTED",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        ),
                    ),
                    MaterialTheme.shapes.medium,
                )
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary),
                )

                Column(modifier = Modifier.padding(BeamsyncSpacing.space4)) {
                    DetailRow("HOST", connection.host)
                    DetailRow("PORT", connection.port.toString())
                    DetailRow("SCHEME", connection.scheme.uppercase())
                    DetailRow("TOKEN", connection.token.take(16) + "…")
                    DetailRow("STATUS", "ONLINE")
                }
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                            MaterialTheme.colorScheme.tertiaryContainer,
                        ),
                    ),
                    MaterialTheme.shapes.medium,
                )
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                .padding(BeamsyncSpacing.space4),
        ) {
            Column {
                Text(
                    text = "READY FOR TRANSFER",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                )
                Spacer(Modifier.height(BeamsyncSpacing.space2))
                Text(
                    text = "BeamSync Desktop is ready. Use the tabs below to send or receive files.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

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
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp,
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )
    }
}
