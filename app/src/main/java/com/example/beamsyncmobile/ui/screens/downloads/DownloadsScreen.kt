package com.example.beamsyncmobile.ui.screens.downloads

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.beamsyncmobile.network.SavePathManager
import com.example.beamsyncmobile.ui.components.ConnectingOverlay
import com.example.beamsyncmobile.ui.components.ScannerOverlay
import com.example.beamsyncmobile.ui.screens.scan.CameraPreview
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@ExperimentalGetImage
@Composable
fun DownloadsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReceiveViewModel = viewModel(),
) {
    val context = LocalContext.current
    val receiveState by viewModel.state.collectAsState()
    val receivedFiles by viewModel.receivedFiles.collectAsState()
    val torchEnabled = remember { mutableStateOf(false) }

    var showSaveDialog by remember { mutableStateOf(true) }
    var saveLocationVersion by remember { mutableIntStateOf(0) }
    val saveLocationLabel = remember(saveLocationVersion) {
        SavePathManager.getModeLabel(context, SavePathManager.getMode(context))
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
            SavePathManager.setCustomPathUri(context, uri.toString())
            SavePathManager.setMode(context, SavePathManager.PATH_CUSTOM)
            saveLocationVersion++
        }
    }

    fun onSaveModeSelected(mode: String) {
        if (mode == SavePathManager.PATH_CUSTOM) {
            folderPicker.launch(null)
        } else {
            SavePathManager.setMode(context, mode)
            saveLocationVersion++
        }
        showSaveDialog = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        when (val state = receiveState) {
            is ReceiveState.Idle -> IdleContent(
                filesCount = receivedFiles.size,
                saveLocationLabel = saveLocationLabel,
                onStartScan = viewModel::startScanning,
                onClearHistory = if (receivedFiles.isNotEmpty()) viewModel::clearHistory else null,
                onChangeSaveLocation = { showSaveDialog = true },
            )
            is ReceiveState.Scanning -> ScanningContent(
                torchEnabled = torchEnabled.value,
                onToggleTorch = { torchEnabled.value = !torchEnabled.value },
                onCancel = viewModel::stopScanning,
                onQrScanned = viewModel::connectToUrl,
                onAnalyze = viewModel::analyzeImage,
            )
            is ReceiveState.Connecting -> ConnectingOverlay(
                label = "Connecting to desktop...",
                onCancel = viewModel::terminate,
            )
            is ReceiveState.FileList -> FileListContent(
                files = state.files,
                connectionInfo = "${state.connection.host}:${state.connection.port}",
                downloadedNames = state.downloadedNames,
                downloadingName = state.downloadingName,
                bytesReceived = state.bytesReceived,
                totalBytes = state.totalBytes,
                saveLocationLabel = saveLocationLabel,
                onDownloadFile = viewModel::startDownload,
                onDownloadAll = viewModel::downloadAll,
                onCancel = viewModel::terminate,
                onChangeSaveLocation = { showSaveDialog = true },
            )
            is ReceiveState.Complete -> CompleteContent(
                files = state.files,
                onDone = viewModel::clearHistory,
                onReceiveMore = viewModel::goBackToFileList,
            )
            is ReceiveState.Error -> ErrorContent(
                message = state.message,
                onDismiss = viewModel::dismissError,
            )
        }
    }

    if (showSaveDialog) {
        val currentMode = SavePathManager.getMode(context)
        val currentCustomUri = SavePathManager.getCustomPathUri(context)
        val recentPaths = remember { SavePathManager.getRecentCustomPaths(context) }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save files to",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    SaveLocationOption(
                        label = "App private storage",
                        subtitle = "Only visible inside BeamSync",
                        selected = currentMode == SavePathManager.PATH_APP_DOWNLOADS,
                        onClick = { onSaveModeSelected(SavePathManager.PATH_APP_DOWNLOADS) },
                    )
                    Spacer(Modifier.height(BeamsyncSpacing.space2))
                    SaveLocationOption(
                        label = "Downloads/BeamSync",
                        subtitle = "Visible in your file manager",
                        selected = currentMode == SavePathManager.PATH_PUBLIC_DOWNLOADS,
                        onClick = { onSaveModeSelected(SavePathManager.PATH_PUBLIC_DOWNLOADS) },
                    )
                    Spacer(Modifier.height(BeamsyncSpacing.space2))
                    SaveLocationOption(
                        label = "Custom folder...",
                        subtitle = "Pick any folder on your device",
                        selected = currentMode == SavePathManager.PATH_CUSTOM,
                        onClick = { onSaveModeSelected(SavePathManager.PATH_CUSTOM) },
                    )

                    if (recentPaths.isNotEmpty()) {
                        Spacer(Modifier.height(BeamsyncSpacing.space4))
                        Text(
                            text = "Recent locations",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = BeamsyncSpacing.space2),
                        )
                        Spacer(Modifier.height(BeamsyncSpacing.space2))
                        recentPaths.forEach { uri ->
                            val isSelected = currentMode == SavePathManager.PATH_CUSTOM && currentCustomUri == uri
                            RecentPathRow(
                                label = SavePathManager.getCustomPathLabel(uri),
                                selected = isSelected,
                                onClick = {
                                    SavePathManager.setCustomPathUri(context, uri)
                                    SavePathManager.setMode(context, SavePathManager.PATH_CUSTOM)
                                    showSaveDialog = false
                                },
                            )
                            Spacer(Modifier.height(BeamsyncSpacing.space1))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("DONE")
                }
            },
        )
    }
}

@Composable
private fun SaveLocationOption(
    label: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .padding(BeamsyncSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Spacer(Modifier.width(BeamsyncSpacing.space2))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecentPathRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val iconTint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .background(bg)
            .padding(horizontal = BeamsyncSpacing.space3, vertical = BeamsyncSpacing.space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.FolderOpen,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(BeamsyncSpacing.space2))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun IdleContent(
    filesCount: Int,
    saveLocationLabel: String,
    onStartScan: () -> Unit,
    onClearHistory: (() -> Unit)?,
    onChangeSaveLocation: () -> Unit,
) {
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(primaryContainer)
                .clickable(onClick = onStartScan),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = onPrimaryContainer,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Text(
            text = "Receive from Desktop",
            style = MaterialTheme.typography.titleLarge,
            color = onSurface,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space2))

        Text(
            text = "On your desktop, click Send, then scan the QR code",
            style = MaterialTheme.typography.bodyMedium,
            color = onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space8))

        Button(
            onClick = onStartScan,
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(BeamsyncSpacing.space2))
            Text(
                text = "SCAN QR CODE",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onChangeSaveLocation),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = surfaceContainerHigh),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BeamsyncSpacing.space4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(BeamsyncSpacing.space2))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Save to",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant,
                    )
                    Text(
                        text = saveLocationLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Change",
                    tint = onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        if (onClearHistory != null) {
            Spacer(Modifier.height(BeamsyncSpacing.space4))
            TextButton(onClick = onClearHistory) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(BeamsyncSpacing.space1))
                Text(
                    text = "CLEAR HISTORY  ($filesCount)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

@ExperimentalGetImage
@Composable
private fun ScanningContent(
    torchEnabled: Boolean,
    onToggleTorch: () -> Unit,
    onCancel: () -> Unit,
    onQrScanned: (String) -> Unit,
    onAnalyze: (androidx.camera.core.ImageProxy) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            torchEnabled = torchEnabled,
            onAnalyze = onAnalyze,
        )

        ScannerOverlay(
            hintText = "Point at the QR code on your desktop",
            torchEnabled = torchEnabled,
            onToggleTorch = onToggleTorch,
            onCancel = onCancel,
        )
    }
}

@Composable
private fun FileListContent(
    files: List<SenderFile>,
    connectionInfo: String,
    downloadedNames: Set<String>,
    downloadingName: String?,
    bytesReceived: Long,
    totalBytes: Long,
    saveLocationLabel: String,
    onDownloadFile: (SenderFile) -> Unit,
    onDownloadAll: () -> Unit,
    onCancel: () -> Unit,
    onChangeSaveLocation: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    val allDone = files.all { it.name in downloadedNames }
    val remainingCount = files.size - downloadedNames.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BeamsyncSpacing.space8),
    ) {
        Spacer(Modifier.height(BeamsyncSpacing.space8))

        Text(
            text = "Files Available",
            style = MaterialTheme.typography.titleLarge,
            color = onSurface,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space1))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = primary,
            )
            Spacer(Modifier.width(BeamsyncSpacing.space1))
            Text(
                text = connectionInfo,
                style = MaterialTheme.typography.labelSmall,
                color = onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
            if (downloadedNames.isNotEmpty()) {
                Spacer(Modifier.width(BeamsyncSpacing.space2))
                Text(
                    text = "\u00B7",
                    color = onSurfaceVariant,
                )
                Spacer(Modifier.width(BeamsyncSpacing.space2))
                Text(
                    text = "$remainingCount remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space2))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = onChangeSaveLocation)
                .background(surfaceContainerHigh)
                .padding(horizontal = BeamsyncSpacing.space3, vertical = BeamsyncSpacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = onSurfaceVariant,
            )
            Spacer(Modifier.width(BeamsyncSpacing.space1))
            Text(
                text = "Save to: $saveLocationLabel",
                style = MaterialTheme.typography.labelSmall,
                color = onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space4))

        if (downloadingName != null) {
            val pct = if (totalBytes > 0) {
                (bytesReceived.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = downloadingName,
                        style = MaterialTheme.typography.labelMedium,
                        color = primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${(pct * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(BeamsyncSpacing.space1))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(pct)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(primary),
                    )
                }
                Spacer(Modifier.height(BeamsyncSpacing.space1))
                Text(
                    text = "${formatSize(bytesReceived)} / ${formatSize(totalBytes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
            }

            Spacer(Modifier.height(BeamsyncSpacing.space4))
        }

        if (files.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No files available on desktop",
                    style = MaterialTheme.typography.bodyLarge,
                    color = onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(BeamsyncSpacing.space3),
            ) {
                items(files, key = { it.name }) { file ->
                    val onDownload = remember(file) { { onDownloadFile(file) } }
                    FileCard(
                        file = file,
                        isDownloaded = file.name in downloadedNames,
                        isDownloading = file.name == downloadingName,
                        onDownload = onDownload,
                    )
                }
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space4))

        Button(
            onClick = onDownloadAll,
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
            enabled = !allDone && remainingCount > 0,
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(BeamsyncSpacing.space2))
            Text(
                text = if (allDone) "ALL DOWNLOADED \u2713" else "DOWNLOAD ALL  ($remainingCount)",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space2))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text(
                text = "DISCONNECT",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space4))
    }
}

@Composable
private fun FileCard(
    file: SenderFile,
    isDownloaded: Boolean = false,
    isDownloading: Boolean = false,
    onDownload: () -> Unit,
) {
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = surfaceContainerHigh),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BeamsyncSpacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (isDownloaded) MaterialTheme.colorScheme.primaryContainer
                        else primary.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isDownloaded) MaterialTheme.colorScheme.onPrimaryContainer else primary,
                )
            }

            Spacer(Modifier.width(BeamsyncSpacing.space3))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (isDownloaded) "Saved" else file.sizeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDownloaded) MaterialTheme.colorScheme.primary else onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
            }

            if (!isDownloaded) {
                Button(
                    onClick = onDownload,
                    shape = MaterialTheme.shapes.small,
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    enabled = !isDownloading,
                ) {
                    Text(
                        text = if (isDownloading) "..." else "GET",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadingContent(
    file: SenderFile,
    bytesReceived: Long,
    totalBytes: Long,
    downloadedCount: Int,
    onCancel: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val pct = if (totalBytes > 0) {
        (bytesReceived.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(BeamsyncSpacing.space12))

        Box(
            modifier = Modifier
                .size(140.dp)
                .drawBehind {
                    val strokeWidth = 10.dp.toPx()
                    val circleRadius = (size.minDimension - strokeWidth) / 2f
                    val center = this.center

                    drawCircle(
                        color = surfaceVariant,
                        radius = circleRadius + strokeWidth / 2f,
                    )
                    drawArc(
                        color = primary,
                        startAngle = -90f,
                        sweepAngle = 360f * pct,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = androidx.compose.ui.geometry.Offset(
                            center.x - circleRadius,
                            center.y - circleRadius,
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            circleRadius * 2,
                            circleRadius * 2,
                        ),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(pct * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = onSurface,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Text(
            text = file.name,
            style = MaterialTheme.typography.titleMedium,
            color = onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Text(
            text = "${formatSize(bytesReceived)} / ${formatSize(totalBytes)}",
            style = MaterialTheme.typography.bodyMedium,
            color = onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        if (downloadedCount > 0) {
            Text(
                text = "$downloadedCount file(s) already saved",
                style = MaterialTheme.typography.labelSmall,
                color = onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(primary),
            )
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(BeamsyncSpacing.space2))
            Text(
                text = "CANCEL",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))
    }
}

@Composable
private fun CompleteContent(
    files: List<ReceivedFile>,
    onDone: () -> Unit,
    onReceiveMore: () -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(BeamsyncSpacing.space12))

        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space4))

        Text(
            text = "Download Complete",
            style = MaterialTheme.typography.headlineSmall,
            color = onSurface,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space2))

        Text(
            text = "${files.size} file(s) saved to your device",
            style = MaterialTheme.typography.bodyMedium,
            color = onSurfaceVariant,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space8))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(BeamsyncSpacing.space2),
        ) {
            items(files, key = { it.uri.toString() }) { file ->
                ReceivedFileItem(file = file)
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space4))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = "DONE",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space2))

        OutlinedButton(
            onClick = onReceiveMore,
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = "RECEIVE MORE FILES",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))
    }
}

@Composable
private fun ReceivedFileItem(file: ReceivedFile) {
    val context = LocalContext.current
    val dateStr = remember(file.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(file.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BeamsyncSpacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(Modifier.width(BeamsyncSpacing.space3))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(BeamsyncSpacing.space2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatSize(file.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(
                        text = "\u00B7",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(file.uri, resolveMimeType(file.name))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, "Open with"))
                    } catch (_: Exception) { }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Open file",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space4))
        Text(
            text = "Connection Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space3))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space8))
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text("DISMISS", style = MaterialTheme.typography.labelLarge)
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024L * 1024 * 1024 -> "${"%.1f".format(bytes.toDouble() / (1024 * 1024))} MB"
        else -> "${"%.2f".format(bytes.toDouble() / (1024L * 1024 * 1024))} GB"
    }
}

private fun resolveMimeType(fileName: String): String {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "pdf" -> "application/pdf"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "bmp" -> "image/bmp"
        "mp4" -> "video/mp4"
        "mkv" -> "video/x-matroska"
        "webm" -> "video/webm"
        "mov" -> "video/quicktime"
        "avi" -> "video/x-msvideo"
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "ogg" -> "audio/ogg"
        "flac" -> "audio/flac"
        "zip" -> "application/zip"
        "rar" -> "application/vnd.rar"
        "doc", "docx" -> "application/msword"
        "txt" -> "text/plain"
        "html", "htm" -> "text/html"
        "apk" -> "application/vnd.android.package-archive"
        else -> "application/octet-stream"
    }
}
