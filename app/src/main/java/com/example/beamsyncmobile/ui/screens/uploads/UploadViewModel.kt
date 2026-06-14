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
)

sealed class UploadState {
    data object Idle : UploadState()
    data object Ready : UploadState()
    data class Uploading(
        val overallProgress: Float = 0f,
        val currentFile: String = "",
        val fileProgress: Float = 0f,
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
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                        val name = if (nameIndex >= 0) it.getString(nameIndex) else "unknown"
                        val size = if (sizeIndex >= 0) it.getLong(sizeIndex) else -1L
                        SelectedFile(name = name, uri = uri, size = size)
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

        val contentResolver = getApplication<Application>().contentResolver
        val specs = files.map { UploadFileSpec(name = it.name, uri = it.uri, size = it.size) }

        _uploadState.value = UploadState.Uploading()

        uploadJob = viewModelScope.launch {
            val result = client.upload(
                conn = conn,
                files = specs,
                contentResolver = contentResolver,
                onFileProgress = { fileName, progress ->
                    _uploadState.value = UploadState.Uploading(
                        currentFile = fileName,
                        fileProgress = progress,
                    )
                },
                onFileComplete = { _ -> },
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
