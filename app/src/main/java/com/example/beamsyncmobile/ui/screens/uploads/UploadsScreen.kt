package com.example.beamsyncmobile.ui.screens.uploads

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.beamsyncmobile.network.CurrentConnection
import androidx.compose.foundation.BorderStroke
import com.example.beamsyncmobile.ui.components.AnimatedSuccessContent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import com.example.beamsyncmobile.ui.components.BeamsyncCircularProgress
import com.example.beamsyncmobile.ui.components.BeamsyncProgressBar
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close

private val MIME_TYPES = arrayOf("*/*")

@Composable
fun UploadsScreen(
    modifier: Modifier = Modifier,
    viewModel: UploadViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val files by viewModel.files.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addFiles(uris)
        }
    }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addFiles(uris)
        }
    }

    val openFile: (SelectedFile) -> Unit = remember {
        { file ->
            val mime = resolveMimeType(file.name, file.mimeType)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(file.uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(Intent.createChooser(intent, null))
            } catch (_: Exception) {
                // No app can handle this file type
            }
        }
    }

    DisposableEffect(Unit) {
        if (CurrentConnection.connection != null) {
            viewModel.startHeartbeat()
        }
        onDispose { viewModel.stopHeartbeat() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        ConnectionBar(status = connectionStatus)

        AnimatedContent(
            targetState = uploadState::class.qualifiedName,
            transitionSpec = { fadeIn(tween(200)).togetherWith(fadeOut(tween(200))) },
            label = "upload-state",
            modifier = Modifier.weight(1f),
        ) { _ ->
            when (val state = uploadState) {
                is UploadState.Idle -> IdleContent(                    onPickFiles = { filePicker.launch(MIME_TYPES) },
                    onPickGallery = { galleryPicker.launch("image/*") })
                is UploadState.Ready -> FileSelectionContent(
                    files = files,
                    onPickFiles = { filePicker.launch(MIME_TYPES) },
                    onRemoveFile = { viewModel.removeFile(it) },
                    onUpload = { viewModel.startUpload() },
                    onPickGallery = { galleryPicker.launch("image/*") },
                    onOpenFile = openFile,
                    onTerminate = viewModel::terminate,
                )
                is UploadState.Uploading -> UploadingContent(
                    state = state,
                    onTerminate = viewModel::terminate,
                )
                is UploadState.Complete -> CompleteContent(
                    onSendMore = { viewModel.reset() },
                    onTerminate = viewModel::terminate,
                )
                is UploadState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.reset() },
                    onTerminate = viewModel::terminate,
                )
            }
        }
    }
}

@Composable
private fun ConnectionBar(status: String) {
    val isConnected = status == "CONNECTED"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
            .padding(horizontal = BeamsyncSpacing.space4, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BeamsyncSpacing.space2),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        MaterialTheme.shapes.small,
                    ),
            )
            Text(
                text = "DESKTOP: $status",
                color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun IdleContent(
    onPickFiles: () -> Unit,
    onPickGallery: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "SEND FILES",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space3))
        Text(
            text = "Select files to transfer\nto your desktop",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space8))
        Button(
            onClick = onPickFiles,
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = "SELECT FILES",
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Spacer(Modifier.height(BeamsyncSpacing.space3))
        OutlinedButton(
            onClick = onPickGallery,
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = "FROM GALLERY",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun FileSelectionContent(
    files: List<SelectedFile>,
    onPickFiles: () -> Unit,
    onRemoveFile: (Int) -> Unit,
    onUpload: () -> Unit,
    onPickGallery: () -> Unit,
    onOpenFile: (SelectedFile) -> Unit,
    onTerminate: () -> Unit,
) {
    val totalSize = files.sumOf { it.size }
    val sizeText = formatBytes(totalSize)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(BeamsyncSpacing.space8),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "SEND FILES",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${files.size} files · $sizeText total",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(BeamsyncSpacing.space2)) {
                TextButton(onClick = onPickGallery) {
                    Text(
                        text = "GALLERY",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                TextButton(onClick = onPickFiles) {
                    Text(
                        text = "ADD",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(BeamsyncSpacing.space2),
            ) {
                itemsIndexed(files, key = { _, file -> file.uri.toString() }) { index, file ->
                    FileRow(
                        file = file,
                        onRemove = { onRemoveFile(index) },
                        onOpenFile = { onOpenFile(file) },
                    )
                }
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space4))

        Button(
            onClick = onUpload,
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = "UPLOAD ${files.size} FILE${if (files.size != 1) "S" else ""}",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space2))

        OutlinedButton(
            onClick = onTerminate,
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
                text = "DISCONNECT",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun FileRow(
    file: SelectedFile,
    onRemove: () -> Unit,
    onOpenFile: () -> Unit,
) {
    val context = LocalContext.current

    val request = remember(key1 = file.uri) {
        if (file.isImage) {
            ImageRequest.Builder(context)
                .data(file.uri)
                .size(160)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(false)
                .build()
        } else null
    }

    val openFile = onOpenFile

    val isImage = file.isImage
    val displayName = file.name
    val displaySize = remember(file.size) { formatBytes(file.size) }

    val surface = MaterialTheme.colorScheme.surface
    val surfaceContainerLow = MaterialTheme.colorScheme.surfaceContainerLow
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val shape = MaterialTheme.shapes.medium

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = openFile),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = surface),
        border = BorderStroke(1.dp, outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = BeamsyncSpacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(shape)
                    .background(surfaceContainerLow),
                contentAlignment = Alignment.Center,
            ) {
                if (isImage && request != null) {
                    AsyncImage(
                        model = request,
                        contentDescription = displayName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = "F",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            Spacer(Modifier.width(BeamsyncSpacing.space3))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = displaySize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove file",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun UploadingContent(
    state: UploadState.Uploading,
    onTerminate: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BeamsyncSpacing.space8),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BeamsyncCircularProgress(
                progress = state.overallProgress,
                size = 160.dp,
                strokeWidth = 8.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(state.overallProgress * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatBytes(state.transferredBytes) + " / " + formatBytes(state.totalBytes),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            Spacer(Modifier.height(BeamsyncSpacing.space4))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.speedBytesPerSec > 0) {
                    Text(
                        text = formatSpeed(state.speedBytesPerSec),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "\u00B7",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (state.etaSeconds > 0) {
                    Text(
                        text = "ETA ${formatEta(state.etaSeconds)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            Spacer(Modifier.height(BeamsyncSpacing.space6))

            if (state.currentFile.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                        .padding(BeamsyncSpacing.space4),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = state.currentFile,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(BeamsyncSpacing.space3))
                            Text(
                                text = "${(state.fileProgress * 100).toInt()}%",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                        Spacer(Modifier.height(BeamsyncSpacing.space2))
                        BeamsyncProgressBar(progress = state.fileProgress)
                        Spacer(Modifier.height(BeamsyncSpacing.space2))
                        Text(
                            text = "${state.filesCompleted} / ${state.totalFiles} files transferred",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onTerminate,
            modifier = Modifier
                .align(Alignment.BottomCenter)
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
                text = "TERMINATE",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun CompleteContent(
    onSendMore: () -> Unit,
    onTerminate: () -> Unit,
) {
    var showButton by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedSuccessContent(
            title = "UPLOAD COMPLETE",
            subtitle = "Files transferred successfully\nto your desktop",
            onComplete = { showButton = true },
        )
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing))
                .plus(slideInVertically(
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 2 },
                )),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = BeamsyncSpacing.space8),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onSendMore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BeamsyncSpacing.space12),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = "SEND MORE FILES",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(Modifier.height(BeamsyncSpacing.space2))
                OutlinedButton(
                    onClick = onTerminate,
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
                        text = "DISCONNECT",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onTerminate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "UPLOAD FAILED",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
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
        Spacer(Modifier.height(BeamsyncSpacing.space2))
        OutlinedButton(
            onClick = onTerminate,
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
                text = "DISCONNECT",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

private fun resolveMimeType(fileName: String, providerMime: String): String {
    if (providerMime.isNotBlank() &&
        providerMime != "application/octet-stream" &&
        providerMime != "*/*"
    ) return providerMime

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
        "gz" -> "application/gzip"
        "tar" -> "application/x-tar"
        "7z" -> "application/x-7z-compressed"
        "doc", "docx" -> "application/msword"
        "xls", "xlsx" -> "application/vnd.ms-excel"
        "ppt", "pptx" -> "application/vnd.ms-powerpoint"
        "txt" -> "text/plain"
        "csv" -> "text/csv"
        "json" -> "application/json"
        "html", "htm" -> "text/html"
        "apk" -> "application/vnd.android.package-archive"
        else -> providerMime.ifBlank { "application/octet-stream" }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes.toDouble() / (1024 * 1024))} MB"
        else -> "${"%.2f".format(bytes.toDouble() / (1024 * 1024 * 1024))} GB"
    }
}

private fun formatSpeed(bytesPerSec: Long): String {
    return when {
        bytesPerSec < 1024 -> "$bytesPerSec B/s"
        bytesPerSec < 1024 * 1024 -> "${bytesPerSec / 1024} KB/s"
        else -> "${"%.1f".format(bytesPerSec.toDouble() / (1024 * 1024))} MB/s"
    }
}

private fun formatEta(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
}
