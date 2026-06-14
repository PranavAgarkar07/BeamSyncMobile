package com.example.beamsyncmobile.ui.screens.uploads

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.beamsyncmobile.network.BeamSyncClient
import com.example.beamsyncmobile.network.CurrentConnection
import com.example.beamsyncmobile.network.UploadFileSpec
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SelectedFile(
    val name: String,
    val uri: Uri,
    val size: Long,
    val mimeType: String = "application/octet-stream",
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
}

sealed class UploadState {
    data object Idle : UploadState()
    data object Ready : UploadState()
    data class Uploading(
        val overallProgress: Float = 0f,
        val currentFile: String = "",
        val fileProgress: Float = 0f,
        val transferredBytes: Long = 0L,
        val totalBytes: Long = 0L,
        val speedBytesPerSec: Long = 0L,
        val filesCompleted: Int = 0,
        val totalFiles: Int = 0,
        val etaSeconds: Long = 0L,
    ) : UploadState()
    data object Complete : UploadState()
    data class Error(val message: String) : UploadState()
}

class UploadViewModel(application: Application) : AndroidViewModel(application) {

    private val client = BeamSyncClient()

    private val _files = MutableStateFlow<List<SelectedFile>>(emptyList())
    val files: StateFlow<List<SelectedFile>> = _files.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val _connectionStatus = MutableStateFlow("DISCONNECTED")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private var heartbeatJob: Job? = null
    private var uploadJob: Job? = null

    fun addFiles(uris: List<Uri>) {
        val contentResolver = getApplication<Application>().contentResolver
        val newFiles = uris.mapNotNull { uri ->
            try {
                val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                        val name = if (nameIndex >= 0) it.getString(nameIndex) else "unknown"
                        val size = if (sizeIndex >= 0) it.getLong(sizeIndex) else -1L
                        SelectedFile(name = name, uri = uri, size = size, mimeType = mimeType)
                    } else null
                }
            } catch (e: Exception) {
                null
            }
        }
        _files.value = _files.value + newFiles
        if (_files.value.isNotEmpty() && _uploadState.value is UploadState.Idle) {
            _uploadState.value = UploadState.Ready
        }
    }

    fun removeFile(index: Int) {
        val updated = _files.value.toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _files.value = updated
        }
        if (_files.value.isEmpty()) {
            _uploadState.value = UploadState.Idle
        }
    }

    fun clearFiles() {
        _files.value = emptyList()
        _uploadState.value = UploadState.Idle
    }

    fun startUpload() {
        val conn = CurrentConnection.connection ?: run {
            _uploadState.value = UploadState.Error("Not connected to any device")
            return
        }
        val files = _files.value
        if (files.isEmpty()) return

        val totalBytes = files.sumOf { it.size }
        val fileSizes = files.map { it.size }
        val contentResolver = getApplication<Application>().contentResolver
        val specs = files.map { UploadFileSpec(name = it.name, uri = it.uri, size = it.size) }

        var filesCompleted = 0
        var transferredBeforeCurrent = 0L
        var smoothedSpeed = 0.0
        var lastBytes = 0L
        var lastTime = System.nanoTime()
        var lastEmitTime = 0L
        val alpha = 0.3

        fun updateProgress(fileName: String, fileProgress: Float, fileSize: Long) {
            val transferred = transferredBeforeCurrent + (fileSize * fileProgress).toLong()
            val overallProgress = if (totalBytes > 0) transferred.toFloat() / totalBytes.toFloat() else 0f

            val now = System.nanoTime()
            val elapsed = now - lastTime
            if (elapsed > 250_000_000L && transferred > lastBytes) {
                val instantSpeed = (transferred - lastBytes).toDouble() / (elapsed / 1_000_000_000.0)
                smoothedSpeed = alpha * instantSpeed + (1.0 - alpha) * smoothedSpeed
                lastBytes = transferred
                lastTime = now
            }

            if (now - lastEmitTime < 100_000_000L) return
            lastEmitTime = now

            val speed = smoothedSpeed.toLong()
            val eta = if (speed > 0) (totalBytes - transferred) / speed else 0L

            _uploadState.value = UploadState.Uploading(
                overallProgress = overallProgress,
                currentFile = fileName,
                fileProgress = fileProgress,
                transferredBytes = transferred,
                totalBytes = totalBytes,
                speedBytesPerSec = speed,
                filesCompleted = filesCompleted,
                totalFiles = files.size,
                etaSeconds = eta,
            )
        }

        _uploadState.value = UploadState.Uploading(
            totalBytes = totalBytes,
            totalFiles = files.size,
        )

        uploadJob = viewModelScope.launch {
            val result = client.upload(
                conn = conn,
                files = specs,
                contentResolver = contentResolver,
                onFileProgress = { fileName, progress ->
                    val idx = specs.indexOfFirst { it.name == fileName }
                    val fileSize = if (idx >= 0) fileSizes[idx] else 0L
                    updateProgress(fileName, progress, fileSize)
                },
                onFileComplete = { fileName ->
                    val idx = specs.indexOfFirst { it.name == fileName }
                    if (idx >= 0) {
                        transferredBeforeCurrent += fileSizes[idx]
                    }
                    filesCompleted++
                },
            )

            result.fold(
                onSuccess = {
                    _uploadState.value = UploadState.Complete
                },
                onFailure = { error ->
                    _uploadState.value = UploadState.Error(
                        error.message ?: "Upload failed"
                    )
                },
            )
        }
    }

    fun reset() {
        uploadJob?.cancel()
        _files.value = emptyList()
        _uploadState.value = UploadState.Idle
    }

    fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (isActive) {
                val conn = CurrentConnection.connection
                if (conn != null) {
                    val result = client.heartbeat(conn)
                    _connectionStatus.value = if (result.isSuccess) "CONNECTED" else "LOST"
                } else {
                    _connectionStatus.value = "DISCONNECTED"
                    break
                }
                delay(5000)
            }
        }
    }

    fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    override fun onCleared() {
        super.onCleared()
        heartbeatJob?.cancel()
        uploadJob?.cancel()
    }
}
