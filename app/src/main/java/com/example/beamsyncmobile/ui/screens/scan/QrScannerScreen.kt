package com.example.beamsyncmobile.ui.screens.scan

import androidx.activity.compose.BackHandler
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.R
import com.example.beamsyncmobile.network.ServerConnection
import com.example.beamsyncmobile.ui.components.ConnectingOverlay
import com.example.beamsyncmobile.ui.components.ScannerOverlay
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

@OptIn(ExperimentalGetImage::class)
@Composable
fun QrScannerScreen(
    modifier: Modifier = Modifier,
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
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (contentState) {
            is ScannerState.Scanning -> {
                BackHandler(onBack = viewModel::reset)

                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    torchEnabled = torchEnabled,
                    onAnalyze = { imageProxy -> viewModel.analyzeImage(imageProxy) },
                )

                ScannerOverlay(
                    hintText = "Align QR code within the frame",
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
            ConnectingOverlay(label = "Connecting...", onCancel = viewModel::reset)
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
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Transfer files seamlessly",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space8))

        Button(
            onClick = onStartCamera,
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = "START CAMERA",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            Spacer(Modifier.width(BeamsyncSpacing.space3))
            Text(
                text = "or enter URL manually",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.width(BeamsyncSpacing.space3))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            singleLine = true,
            isError = manualError != null,
            supportingText = if (manualError != null) {
                { Text(text = manualError, color = MaterialTheme.colorScheme.error) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                errorBorderColor = MaterialTheme.colorScheme.error,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
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
            shape = MaterialTheme.shapes.medium,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                onManualSubmit()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = manualUrl.isNotBlank(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = "CONNECT",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        if (recentConnections.isNotEmpty()) {
            Spacer(Modifier.height(BeamsyncSpacing.space8))

            Text(
                text = "RECENT CONNECTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
            )

            Spacer(Modifier.height(BeamsyncSpacing.space2))

            recentConnections.forEach { rc ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onConnectRecent(rc) }
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Monospace,
                            )
                            Text(
                                text = "Tap to reconnect",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "CONNECT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }
                }
                Spacer(Modifier.height(BeamsyncSpacing.space1))
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space8))
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
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "CONNECTION FAILED",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space8))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = "TRY AGAIN",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            Spacer(Modifier.width(BeamsyncSpacing.space3))
            Text(
                text = "or enter URL manually",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.width(BeamsyncSpacing.space3))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            singleLine = true,
            isError = manualError != null,
            supportingText = if (manualError != null) {
                { Text(text = manualError, color = MaterialTheme.colorScheme.error) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                errorBorderColor = MaterialTheme.colorScheme.error,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(
                onGo = { onManualSubmit() },
            ),
            shape = MaterialTheme.shapes.medium,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        OutlinedButton(
            onClick = onManualSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = manualUrl.isNotBlank(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = "CONNECT",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        if (recentConnections.isNotEmpty()) {
            Spacer(Modifier.height(BeamsyncSpacing.space8))

            Text(
                text = "RECENT CONNECTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
            )

            Spacer(Modifier.height(BeamsyncSpacing.space2))

            recentConnections.forEach { rc ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onConnectRecent(rc) }
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Monospace,
                            )
                            Text(
                                text = "Tap to reconnect",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "CONNECT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }
                }
                Spacer(Modifier.height(BeamsyncSpacing.space1))
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space8))
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
            .statusBarsPadding()
            .navigationBarsPadding()
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

        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = stringResource(R.string.open_settings),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        TextButton(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.try_again),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
