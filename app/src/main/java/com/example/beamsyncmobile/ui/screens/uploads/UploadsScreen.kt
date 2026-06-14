package com.example.beamsyncmobile.ui.screens.uploads

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.beamsyncmobile.network.CurrentConnection
import com.example.beamsyncmobile.ui.components.BeamsyncButton
import com.example.beamsyncmobile.ui.components.BeamsyncButtonSize
import com.example.beamsyncmobile.ui.components.BeamsyncButtonVariant
import com.example.beamsyncmobile.ui.components.BeamsyncProgressBar
import com.example.beamsyncmobile.ui.theme.BeamsyncColors
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

private val MIME_TYPES = arrayOf("*/*")

@Composable
fun UploadsScreen(
    viewModel: UploadViewModel = viewModel(),
) {
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

    LaunchedEffect(Unit) {
        if (CurrentConnection.connection != null) {
            viewModel.startHeartbeat()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeamsyncColors.surfaceBase),
    ) {
        ConnectionBar(status = connectionStatus)

        AnimatedContent(
            targetState = uploadState,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            label = "upload-state",
        ) { state ->
            when (state) {
                is UploadState.Idle -> IdleContent(onPickFiles = { filePicker.launch(MIME_TYPES) })
                is UploadState.Ready -> FileSelectionContent(
                    files = files,
                    onPickFiles = { filePicker.launch(MIME_TYPES) },
                    onRemoveFile = { viewModel.removeFile(it) },
                    onUpload = { viewModel.startUpload() },
                )
                is UploadState.Uploading -> UploadingContent(
                    currentFile = state.currentFile,
                    fileProgress = state.fileProgress,
                )
                is UploadState.Complete -> CompleteContent(
                    onSendMore = { viewModel.reset() },
                )
                is UploadState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.reset() },
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
            .background(if (isConnected) BeamsyncColors.surfacePositive.copy(alpha = 0.15f) else BeamsyncColors.surfaceCritical.copy(alpha = 0.1f))
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
                        if (isConnected) BeamsyncColors.surfacePositive else BeamsyncColors.surfaceCritical,
                        RoundedCornerShape(0.dp),
                    ),
            )
            Text(
                text = "DESKTOP: $status",
                color = if (isConnected) BeamsyncColors.surfacePositive else BeamsyncColors.surfaceCritical,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun IdleContent(onPickFiles: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "SEND FILES",
            color = BeamsyncColors.textPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space3))
        Text(
            text = "Select files to transfer\nto your desktop",
            color = BeamsyncColors.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space8))
        BeamsyncButton(
            text = "SELECT FILES",
            variant = BeamsyncButtonVariant.Primary,
            size = BeamsyncButtonSize.Large,
            fullWidth = true,
            onClick = onPickFiles,
        )
    }
}

@Composable
private fun FileSelectionContent(
    files: List<SelectedFile>,
    onPickFiles: () -> Unit,
    onRemoveFile: (Int) -> Unit,
    onUpload: () -> Unit,
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
                    color = BeamsyncColors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${files.size} files · $sizeText total",
                    color = BeamsyncColors.textSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
            BeamsyncButton(
                text = "+ ADD",
                variant = BeamsyncButtonVariant.Ghost,
                size = BeamsyncButtonSize.Small,
                onClick = onPickFiles,
            )
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
                itemsIndexed(files) { index, file ->
                    FileRow(
                        name = file.name,
                        size = file.size,
                        onRemove = { onRemoveFile(index) },
                    )
                }
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space4))

        BeamsyncButton(
            text = "UPLOAD ${files.size} FILE${if (files.size != 1) "S" else ""}",
            variant = BeamsyncButtonVariant.Primary,
            size = BeamsyncButtonSize.Large,
            fullWidth = true,
            onClick = onUpload,
        )
    }
}

@Composable
private fun FileRow(
    name: String,
    size: Long,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(BeamsyncColors.surfaceRaised)
            .border(1.dp, BeamsyncColors.strokeDefault, RoundedCornerShape(0.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = BeamsyncSpacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(BeamsyncColors.surfaceHigher),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "F",
                    color = BeamsyncColors.accentPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
            }

            Spacer(Modifier.width(BeamsyncSpacing.space3))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = BeamsyncColors.textPrimary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatBytes(size),
                    color = BeamsyncColors.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove file",
                    tint = BeamsyncColors.textSecondary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun UploadingContent(
    currentFile: String,
    fileProgress: Float,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "UPLOADING",
            color = BeamsyncColors.accentPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space8))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BeamsyncColors.surfaceRaised)
                .border(1.dp, BeamsyncColors.strokeDefault, RoundedCornerShape(0.dp))
                .padding(BeamsyncSpacing.space4),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = currentFile,
                    color = BeamsyncColors.textPrimary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(BeamsyncSpacing.space3))
                BeamsyncProgressBar(progress = fileProgress)
                Spacer(Modifier.height(BeamsyncSpacing.space2))
                Text(
                    text = "${(fileProgress * 100).toInt()}%",
                    color = BeamsyncColors.textSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun CompleteContent(onSendMore: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "UPLOAD COMPLETE",
            color = BeamsyncColors.surfacePositive,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space3))
        Text(
            text = "Files transferred successfully\nto your desktop",
            color = BeamsyncColors.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space8))
        BeamsyncButton(
            text = "SEND MORE FILES",
            variant = BeamsyncButtonVariant.Primary,
            size = BeamsyncButtonSize.Large,
            fullWidth = true,
            onClick = onSendMore,
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
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
            color = BeamsyncColors.surfaceCritical,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
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
