package com.example.beamsyncmobile.ui.screens.scan

import androidx.activity.compose.BackHandler
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.R
import com.example.beamsyncmobile.network.ServerConnection
import com.example.beamsyncmobile.ui.components.BeamsyncButton
import com.example.beamsyncmobile.ui.components.BeamsyncButtonSize
import com.example.beamsyncmobile.ui.components.BeamsyncButtonVariant
import com.example.beamsyncmobile.ui.theme.BeamsyncColors
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

private val ScreenBg = Color(0xFFFEF9F0)

@OptIn(ExperimentalGetImage::class)
@Composable
fun QrScannerScreen(
    viewModel: QrScannerViewModel,
    onConnected: (ServerConnection) -> Unit,
) {
    val scannerState by viewModel.scannerState.collectAsState()
    val manualUrl by viewModel.manualUrl.collectAsState()
    val manualError by viewModel.manualError.collectAsState()
    val torchEnabled by viewModel.torchEnabled.collectAsState()
    val recentConnections by viewModel.recentConnections.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.connectEvent.collect { connection ->
            onConnected(connection)
        }
    }

    var contentState by remember { mutableStateOf<ScannerState>(ScannerState.Waiting) }
    LaunchedEffect(scannerState) {
        if (scannerState !is ScannerState.Connecting) {
            contentState = scannerState
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg),
    ) {
        when (contentState) {
            is ScannerState.Scanning -> {
                BackHandler(onBack = viewModel::reset)

                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    torchEnabled = torchEnabled,
                    onAnalyze = { imageProxy -> viewModel.analyzeImage(imageProxy) },
                )

                ScanningOverlay(
                    torchEnabled = torchEnabled,
                    onToggleTorch = viewModel::toggleTorch,
                    onCancel = viewModel::reset,
                )
            }

            is ScannerState.Error -> {
                ErrorContent(
                    message = (contentState as ScannerState.Error).message,
                    manualUrl = manualUrl,
                    onManualUrlChanged = viewModel::onManualUrlChanged,
                    onManualSubmit = viewModel::connectManual,
                    manualError = manualError,
                    recentConnections = recentConnections,
                    onConnectRecent = viewModel::connectToRecent,
                    onRetry = viewModel::reset,
                    scrollState = scrollState,
                )
            }

            is ScannerState.Denied -> {
                val ctx = LocalContext.current
                DeniedContent(
                    onOpenSettings = {
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.fromParts("package", ctx.packageName, null),
                        )
                        ctx.startActivity(intent)
                    },
                    onRetry = viewModel::reset,
                )
            }

            else -> {
                WaitingContent(
                    manualUrl = manualUrl,
                    onManualUrlChanged = viewModel::onManualUrlChanged,
                    onManualSubmit = viewModel::connectManual,
                    manualError = manualError,
                    onStartCamera = viewModel::onCameraReady,
                    recentConnections = recentConnections,
                    onConnectRecent = viewModel::connectToRecent,
                    scrollState = scrollState,
                )
            }
        }

        if (scannerState is ScannerState.Connecting) {
            ConnectingOverlay(onCancel = viewModel::reset)
        }
    }
}

@Composable
private fun WaitingContent(
    manualUrl: String,
    onManualUrlChanged: (String) -> Unit,
    onManualSubmit: () -> Unit,
    manualError: String?,
    onStartCamera: () -> Unit,
    recentConnections: List<RecentConnection>,
    onConnectRecent: (RecentConnection) -> Unit,
    scrollState: androidx.compose.foundation.ScrollState,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.weight(1f))

        Icon(
            painter = painterResource(R.drawable.ic_onboarding_logo),
            contentDescription = "BeamSync",
            tint = Color.Unspecified,
            modifier = Modifier.size(72.dp),
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Text(
            text = "BeamSync",
            style = MaterialTheme.typography.headlineMedium,
            color = BeamsyncColors.textPrimary,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Transfer files seamlessly",
            style = MaterialTheme.typography.bodyMedium,
            color = BeamsyncColors.textSecondary,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space8))

        BeamsyncButton(
            text = "START CAMERA",
            variant = BeamsyncButtonVariant.Primary,
            size = BeamsyncButtonSize.Large,
            fullWidth = true,
            onClick = onStartCamera,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(BeamsyncColors.strokeDefault),
            )
            Spacer(Modifier.width(BeamsyncSpacing.space3))
            Text(
                text = "or enter URL manually",
                color = BeamsyncColors.textSecondary,
                fontSize = 12.sp,
            )
            Spacer(Modifier.width(BeamsyncSpacing.space3))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(BeamsyncColors.strokeDefault),
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space4))

        OutlinedTextField(
            value = manualUrl,
            onValueChange = onManualUrlChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "http://192.168.1.100:3000/?token=...",
                    color = BeamsyncColors.textDisabled,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = BeamsyncColors.textPrimary,
            ),
            singleLine = true,
            isError = manualError != null,
            supportingText = if (manualError != null) {
                { Text(text = manualError, color = BeamsyncColors.surfaceCritical) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BeamsyncColors.strokeActive,
                unfocusedBorderColor = BeamsyncColors.strokeDefault,
                unfocusedContainerColor = BeamsyncColors.surfaceRaised,
                errorBorderColor = BeamsyncColors.surfaceCritical,
                cursorColor = BeamsyncColors.accentPrimary,
                focusedTextColor = BeamsyncColors.textPrimary,
                unfocusedTextColor = BeamsyncColors.textPrimary,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(
                onGo = {
                    focusManager.clearFocus()
                    onManualSubmit()
                },
            ),
            shape = RoundedCornerShape(0.dp),
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        BeamsyncButton(
            text = "CONNECT",
            variant = BeamsyncButtonVariant.DataAction,
            size = BeamsyncButtonSize.Default,
            fullWidth = true,
            enabled = manualUrl.isNotBlank(),
            onClick = {
                focusManager.clearFocus()
                onManualSubmit()
            },
        )

        if (recentConnections.isNotEmpty()) {
            Spacer(Modifier.height(BeamsyncSpacing.space8))

            Text(
                text = "RECENT CONNECTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = BeamsyncColors.textSecondary,
                letterSpacing = 1.sp,
            )

            Spacer(Modifier.height(BeamsyncSpacing.space2))

            recentConnections.forEach { rc ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(0.dp))
                        .clickable { onConnectRecent(rc) }
                        .background(BeamsyncColors.surfaceRaised)
                        .padding(horizontal = BeamsyncSpacing.space4, vertical = BeamsyncSpacing.space3),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = rc.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = BeamsyncColors.textPrimary,
                                fontFamily = FontFamily.Monospace,
                            )
                            Text(
                                text = "Tap to reconnect",
                                style = MaterialTheme.typography.labelSmall,
                                color = BeamsyncColors.textSecondary,
                            )
                        }
                        Text(
                            text = "CONNECT",
                            style = MaterialTheme.typography.labelSmall,
                            color = BeamsyncColors.accentPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }
                }
                Spacer(Modifier.height(BeamsyncSpacing.space1))
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun ScanningOverlay(
    torchEnabled: Boolean,
    onToggleTorch: () -> Unit,
    onCancel: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
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
            // Transparent cutout area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.01f)),
            )

            // Corner brackets
            CornerBracket(Alignment.TopStart)
            CornerBracket(Alignment.TopEnd)
            CornerBracket(Alignment.BottomStart)
            CornerBracket(Alignment.BottomEnd)

            // Scan line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopStart)
                    .offset(y = 258.dp * scanLineProgress)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                BeamsyncColors.accentPrimary,
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }

        // Guidance text below frame
        Text(
            text = "Align QR code within the frame",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 170.dp),
            letterSpacing = 0.5.sp,
        )

        // Scanning label
        Text(
            text = "SCANNING...",
            color = BeamsyncColors.accentPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            textAlign = TextAlign.Center,
        )

        // Cancel button - top left
        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(BeamsyncSpacing.space4),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel scanning",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Cancel",
                color = Color.White,
                fontSize = 14.sp,
            )
        }

        // Torch toggle - bottom right
        IconButton(
            onClick = onToggleTorch,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(BeamsyncSpacing.space6)
                .size(48.dp)
                .background(
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(24.dp),
                ),
        ) {
            Icon(
                imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = if (torchEnabled) "Disable flash" else "Enable flash",
                tint = if (torchEnabled) BeamsyncColors.surfaceWarning else Color.White,
            )
        }
    }
}

@Composable
private fun BoxScope.CornerBracket(alignment: Alignment) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .align(alignment)
            .padding(
                start = if (alignment == Alignment.TopStart || alignment == Alignment.BottomStart) 5.dp else 0.dp,
                end = if (alignment == Alignment.TopEnd || alignment == Alignment.BottomEnd) 5.dp else 0.dp,
                top = if (alignment == Alignment.TopStart || alignment == Alignment.TopEnd) 5.dp else 0.dp,
                bottom = if (alignment == Alignment.BottomStart || alignment == Alignment.BottomEnd) 5.dp else 0.dp,
            ),
    ) {
        Box(
            modifier = Modifier
                .align(alignment)
                .let {
                    when (alignment) {
                        Alignment.TopStart, Alignment.TopEnd -> it.width(30.dp).height(3.dp)
                        else -> it.width(3.dp).height(30.dp)
                    }
                }
                .background(BeamsyncColors.accentPrimary),
        )
        Box(
            modifier = Modifier
                .align(alignment)
                .let {
                    when (alignment) {
                        Alignment.TopStart, Alignment.BottomStart -> it.width(3.dp).height(30.dp)
                        else -> it.width(30.dp).height(3.dp)
                    }
                }
                .background(BeamsyncColors.accentPrimary),
        )
    }
}

@Composable
private fun ConnectingOverlay(onCancel: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                color = BeamsyncColors.accentPrimary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(BeamsyncSpacing.space4))
            Text(
                text = "Connecting...",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(BeamsyncSpacing.space6))
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancel",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    manualUrl: String,
    onManualUrlChanged: (String) -> Unit,
    onManualSubmit: () -> Unit,
    manualError: String?,
    recentConnections: List<RecentConnection>,
    onConnectRecent: (RecentConnection) -> Unit,
    onRetry: () -> Unit,
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
        Spacer(Modifier.weight(1f))

        Text(
            text = "CONNECTION FAILED",
            color = BeamsyncColors.textError,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Text(
            text = message,
            color = BeamsyncColors.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space8))

        BeamsyncButton(
            text = "TRY AGAIN",
            variant = BeamsyncButtonVariant.Primary,
            size = BeamsyncButtonSize.Large,
            fullWidth = true,
            onClick = onRetry,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(BeamsyncColors.strokeDefault),
            )
            Spacer(Modifier.width(BeamsyncSpacing.space3))
            Text(
                text = "or enter URL manually",
                color = BeamsyncColors.textSecondary,
                fontSize = 12.sp,
            )
            Spacer(Modifier.width(BeamsyncSpacing.space3))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(BeamsyncColors.strokeDefault),
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space4))

        OutlinedTextField(
            value = manualUrl,
            onValueChange = onManualUrlChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "http://192.168.1.100:3000/?token=...",
                    color = BeamsyncColors.textDisabled,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = BeamsyncColors.textPrimary,
            ),
            singleLine = true,
            isError = manualError != null,
            supportingText = if (manualError != null) {
                { Text(text = manualError, color = BeamsyncColors.surfaceCritical) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BeamsyncColors.strokeActive,
                unfocusedBorderColor = BeamsyncColors.strokeDefault,
                unfocusedContainerColor = BeamsyncColors.surfaceRaised,
                errorBorderColor = BeamsyncColors.surfaceCritical,
                cursorColor = BeamsyncColors.accentPrimary,
                focusedTextColor = BeamsyncColors.textPrimary,
                unfocusedTextColor = BeamsyncColors.textPrimary,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(
                onGo = { onManualSubmit() },
            ),
            shape = RoundedCornerShape(0.dp),
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        BeamsyncButton(
            text = "CONNECT",
            variant = BeamsyncButtonVariant.DataAction,
            size = BeamsyncButtonSize.Default,
            fullWidth = true,
            enabled = manualUrl.isNotBlank(),
            onClick = onManualSubmit,
        )

        if (recentConnections.isNotEmpty()) {
            Spacer(Modifier.height(BeamsyncSpacing.space8))

            Text(
                text = "RECENT CONNECTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = BeamsyncColors.textSecondary,
                letterSpacing = 1.sp,
            )

            Spacer(Modifier.height(BeamsyncSpacing.space2))

            recentConnections.forEach { rc ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(0.dp))
                        .clickable { onConnectRecent(rc) }
                        .background(BeamsyncColors.surfaceRaised)
                        .padding(horizontal = BeamsyncSpacing.space4, vertical = BeamsyncSpacing.space3),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = rc.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = BeamsyncColors.textPrimary,
                                fontFamily = FontFamily.Monospace,
                            )
                            Text(
                                text = "Tap to reconnect",
                                style = MaterialTheme.typography.labelSmall,
                                color = BeamsyncColors.textSecondary,
                            )
                        }
                        Text(
                            text = "CONNECT",
                            style = MaterialTheme.typography.labelSmall,
                            color = BeamsyncColors.accentPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }
                }
                Spacer(Modifier.height(BeamsyncSpacing.space1))
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun DeniedContent(
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BeamsyncSpacing.space8)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp),
        )

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Text(
            text = stringResource(R.string.camera_permission_denied),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Text(
            text = stringResource(R.string.camera_permission_denied_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space10))

        BeamsyncButton(
            text = stringResource(R.string.open_settings),
            variant = BeamsyncButtonVariant.Secondary,
            size = BeamsyncButtonSize.Large,
            fullWidth = true,
            onClick = onOpenSettings,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        BeamsyncButton(
            text = stringResource(R.string.try_again),
            variant = BeamsyncButtonVariant.Ghost,
            size = BeamsyncButtonSize.Default,
            fullWidth = true,
            onClick = onRetry,
        )
    }
}
