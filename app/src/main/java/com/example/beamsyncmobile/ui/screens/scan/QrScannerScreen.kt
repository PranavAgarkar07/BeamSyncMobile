package com.example.beamsyncmobile.ui.screens.scan

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.network.ServerConnection
import com.example.beamsyncmobile.ui.components.BeamsyncButton
import com.example.beamsyncmobile.ui.components.BeamsyncButtonSize
import com.example.beamsyncmobile.ui.components.BeamsyncButtonVariant
import com.example.beamsyncmobile.ui.theme.BeamsyncColors
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

@OptIn(ExperimentalGetImage::class)
@Composable
fun QrScannerScreen(
    viewModel: QrScannerViewModel,
    onConnected: (ServerConnection) -> Unit,
) {
    val scannerState by viewModel.scannerState.collectAsState()
    val manualUrl by viewModel.manualUrl.collectAsState()
    val manualError by viewModel.manualError.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BeamsyncColors.surfaceBase),
    ) {
        when (scannerState) {
            is ScannerState.Waiting -> {
                ContentColumn(
                    title = "SCAN QR CODE",
                    subtitle = "Point camera at BeamSync desktop QR\nto connect and start transferring",
                    action = {
                        viewModel.onCameraReady()
                    },
                    actionLabel = "START CAMERA",
                    manualUrl = manualUrl,
                    onManualUrlChanged = { viewModel.onManualUrlChanged(it) },
                    onManualSubmit = { viewModel.connectManual() },
                    manualError = manualError,
                    scrollState = scrollState,
                )
            }

            is ScannerState.Scanning -> {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onAnalyze = { imageProxy -> viewModel.analyzeImage(imageProxy) },
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                ) {
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .align(Alignment.Center),
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(40.dp)
                                .padding(top = 5.dp, start = 5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .width(30.dp)
                                    .height(3.dp)
                                    .background(BeamsyncColors.accentPrimary),
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .width(3.dp)
                                    .height(30.dp)
                                    .background(BeamsyncColors.accentPrimary),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(40.dp)
                                .padding(top = 5.dp, end = 5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .width(30.dp)
                                    .height(3.dp)
                                    .background(BeamsyncColors.accentPrimary),
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .width(3.dp)
                                    .height(30.dp)
                                    .background(BeamsyncColors.accentPrimary),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .size(40.dp)
                                .padding(bottom = 5.dp, start = 5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .width(30.dp)
                                    .height(3.dp)
                                    .background(BeamsyncColors.accentPrimary),
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .width(3.dp)
                                    .height(30.dp)
                                    .background(BeamsyncColors.accentPrimary),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(40.dp)
                                .padding(bottom = 5.dp, end = 5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .width(30.dp)
                                    .height(3.dp)
                                    .background(BeamsyncColors.accentPrimary),
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .width(3.dp)
                                    .height(30.dp)
                                    .background(BeamsyncColors.accentPrimary),
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "SCANNING...",
                        color = BeamsyncColors.accentPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                    )
                }
            }

            is ScannerState.Found -> {
                val connection = (scannerState as ScannerState.Found).connection

                LaunchedEffect(connection) {
                    onConnected(connection)
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(BeamsyncSpacing.space8),
                    ) {
                        Text(
                            text = "CONNECTED",
                            color = BeamsyncColors.surfacePositive,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                        )
                        Spacer(Modifier.height(BeamsyncSpacing.space4))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BeamsyncColors.surfaceRaised)
                                .border(1.dp, BeamsyncColors.strokeDefault, RoundedCornerShape(0.dp))
                                .padding(BeamsyncSpacing.space4),
                        ) {
                            Column {
                                ConnectionRow("HOST", connection.host)
                                ConnectionRow("PORT", connection.port.toString())
                                ConnectionRow("TOKEN", connection.token.take(12) + "...")
                                ConnectionRow("SCHEME", connection.scheme.uppercase())
                            }
                        }
                    }
                }
            }

            is ScannerState.Error -> {
                ContentColumn(
                    title = "CONNECTION FAILED",
                    subtitle = (scannerState as ScannerState.Error).message,
                    action = { viewModel.reset() },
                    actionLabel = "TRY AGAIN",
                    manualUrl = manualUrl,
                    onManualUrlChanged = { viewModel.onManualUrlChanged(it) },
                    onManualSubmit = { viewModel.connectManual() },
                    manualError = manualError,
                    scrollState = scrollState,
                )
            }

            is ScannerState.Denied -> {
                ContentColumn(
                    title = "CAMERA PERMISSION REQUIRED",
                    subtitle = "Grant camera access in Settings to scan QR codes",
                    action = { viewModel.reset() },
                    actionLabel = "RETRY",
                    manualUrl = manualUrl,
                    onManualUrlChanged = { viewModel.onManualUrlChanged(it) },
                    onManualSubmit = { viewModel.connectManual() },
                    manualError = manualError,
                    scrollState = scrollState,
                )
            }
        }
    }
}

@Composable
private fun ContentColumn(
    title: String,
    subtitle: String,
    action: () -> Unit,
    actionLabel: String,
    manualUrl: String,
    onManualUrlChanged: (String) -> Unit,
    onManualSubmit: () -> Unit,
    manualError: String?,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            color = BeamsyncColors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space3))
        Text(
            text = subtitle,
            color = BeamsyncColors.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space8))

        BeamsyncButton(
            text = actionLabel,
            variant = BeamsyncButtonVariant.Primary,
            size = BeamsyncButtonSize.Large,
            fullWidth = true,
            onClick = action,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space6))
        Text(
            text = "or enter URL manually",
            color = BeamsyncColors.textSecondary,
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space2))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(BeamsyncColors.surfaceRaised)
                .border(1.dp, if (manualError != null) BeamsyncColors.surfaceCritical else BeamsyncColors.strokeDefault, RoundedCornerShape(0.dp))
                .padding(horizontal = BeamsyncSpacing.space4),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = if (manualUrl.isBlank()) "http://192.168.1.100:3000/?token=..." else manualUrl,
                color = if (manualUrl.isBlank()) BeamsyncColors.textDisabled else BeamsyncColors.textPrimary,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
            )
        }

        if (manualError != null) {
            Text(
                text = manualError,
                color = BeamsyncColors.surfaceCritical,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space3))
        BeamsyncButton(
            text = "CONNECT",
            variant = BeamsyncButtonVariant.DataAction,
            size = BeamsyncButtonSize.Default,
            fullWidth = true,
            enabled = manualUrl.isNotBlank(),
            onClick = onManualSubmit,
        )
    }
}

@Composable
private fun ConnectionRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = BeamsyncColors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
        )
        Text(
            text = value,
            color = BeamsyncColors.textPrimary,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}
