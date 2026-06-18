package com.example.beamsyncmobile.ui.screens.downloads

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import androidx.compose.runtime.Immutable
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.beamsyncmobile.data.history.HistoryRepository
import com.example.beamsyncmobile.data.history.TransferDirection
import com.example.beamsyncmobile.data.history.TransferStatus
import com.example.beamsyncmobile.network.BeamSyncClient
import com.example.beamsyncmobile.network.CurrentConnection
import com.example.beamsyncmobile.network.SavePathManager
import com.example.beamsyncmobile.network.SaveTarget
import com.example.beamsyncmobile.network.SenderFileInfo
import com.example.beamsyncmobile.network.ServerConnection
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Immutable
sealed class ReceiveState {
    data object Idle : ReceiveState()
    data object Scanning : ReceiveState()
    data object Connecting : ReceiveState()
    data class FileList(
        val files: List<SenderFile>,
        val connection: ServerConnection,
        val downloadedNames: Set<String> = emptySet(),
        val downloadingName: String? = null,
        val bytesReceived: Long = 0,
        val totalBytes: Long = 0,
    ) : ReceiveState()
    data class Complete(val files: List<ReceivedFile>) : ReceiveState()
    data class Error(val message: String) : ReceiveState()
}

data class SenderFile(
    val name: String,
    val sizeText: String,
    val downloadUrl: String,
)

data class ReceivedFile(
    val name: String,
    val size: Long,
    val uri: Uri,
    val timestamp: Long,
)

class ReceiveViewModel(application: Application) : AndroidViewModel(application) {

    private val client = BeamSyncClient()
    private val app = application
    private val scanner by lazy { BarcodeScanning.getClient() }

    private val _state = MutableStateFlow<ReceiveState>(ReceiveState.Idle)
    val state: StateFlow<ReceiveState> = _state.asStateFlow()

    private val _receivedFiles = MutableStateFlow<List<ReceivedFile>>(emptyList())
    val receivedFiles: StateFlow<List<ReceivedFile>> = _receivedFiles.asStateFlow()

    private var currentConnection: ServerConnection? = null
    private var currentDownloadUrl: String? = null
    private var isProcessing = false
    private var downloadJob: Job? = null

    fun startScanning() {
        _state.value = ReceiveState.Scanning
    }

    fun stopScanning() {
        _state.value = ReceiveState.Idle
    }

    fun onPermissionDenied() {
        _state.value = ReceiveState.Error("Camera permission is required to scan QR codes")
    }

    fun goBackToFileList() {
        val conn = currentConnection ?: run {
            _state.value = ReceiveState.Idle
            return
        }
        val s = _state.value
        if (s is ReceiveState.Complete) {
            _state.value = ReceiveState.Idle
            return
        }
        val currentFiles = (s as? ReceiveState.FileList)?.files
        if (currentFiles != null) {
            _state.value = ReceiveState.FileList(
                files = currentFiles,
                connection = conn,
                downloadedNames = _receivedFiles.value.map { it.name }.toSet(),
            )
        } else {
            viewModelScope.launch {
                fetchFileList(conn)
            }
        }
    }

    @ExperimentalGetImage
    fun analyzeImage(imageProxy: ImageProxy) {
        if (isProcessing) {
            imageProxy.close()
            return
        }

        @Suppress("UnsafeOptInUsageError")
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        isProcessing = true
                        onQrScanned(rawValue)
                        break
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
                isProcessing = false
            }
    }

    fun connectToUrl(url: String) {
        _state.value = ReceiveState.Connecting
        viewModelScope.launch {
            val result = client.connectToSender(url)
            result.fold(
                onSuccess = { connection ->
                    currentConnection = connection
                    fetchFileList(connection)
                },
                onFailure = { error ->
                    _state.value = ReceiveState.Error(
                        error.message ?: "Could not connect to the desktop. Make sure both devices are on the same Wi-Fi."
                    )
                },
            )
        }
    }

    private suspend fun fetchFileList(connection: ServerConnection) {
        val result = client.fetchSenderFileList(connection)
        result.fold(
            onSuccess = { files ->
                currentConnection = connection
                val conn = connection
                _state.value = ReceiveState.FileList(
                    files = files.map { SenderFile(it.name, it.sizeText, it.downloadUrl) },
                    connection = conn,
                    downloadedNames = _receivedFiles.value.map { it.name }.toSet(),
                )
            },
            onFailure = { error ->
                _state.value = ReceiveState.Error(
                    error.message ?: "Failed to fetch file list from desktop."
                )
            },
        )
    }

    fun startDownload(file: SenderFile) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            try {
                val s = _state.value
                if (s !is ReceiveState.FileList) return@launch
                _state.value = s.copy(downloadingName = file.name, bytesReceived = 0, totalBytes = 0)

                val result = downloadAndSave(file)

                result.fold(
                    onSuccess = { (size, uri) ->
                        val received = ReceivedFile(
                            name = file.name, size = size, uri = uri,
                            timestamp = System.currentTimeMillis(),
                        )
                        _receivedFiles.value = _receivedFiles.value + received
                        HistoryRepository.addRecord(
                            app, file.name, size,
                            TransferDirection.RECEIVE, TransferStatus.SUCCESS,
                        )
                        val st = _state.value
                        if (st is ReceiveState.FileList) {
                            _state.value = st.copy(
                                downloadedNames = st.downloadedNames + file.name,
                                downloadingName = null, bytesReceived = 0, totalBytes = 0,
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.value = ReceiveState.Error(error.message ?: "Download failed")
                    },
                )
            } catch (e: Exception) {
                _state.value = ReceiveState.Error("Download error: ${e.message}")
            }
        }
    }

    fun downloadAll() {
        val currentState = _state.value
        if (currentState !is ReceiveState.FileList) return
        val filesToDownload = currentState.files.filter { it.name !in currentState.downloadedNames }
        if (filesToDownload.isEmpty()) return

        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            try {
                for (file in filesToDownload) {
                    val s = _state.value
                    if (s !is ReceiveState.FileList) return@launch

                    _state.value = s.copy(
                        downloadingName = file.name, bytesReceived = 0, totalBytes = 0,
                    )

                    val result = downloadAndSave(file)

                    result.fold(
                        onSuccess = { (size, uri) ->
                            val received = ReceivedFile(
                                name = file.name, size = size, uri = uri,
                                timestamp = System.currentTimeMillis(),
                            )
                            _receivedFiles.value = _receivedFiles.value + received
                            HistoryRepository.addRecord(
                                app, file.name, size,
                                TransferDirection.RECEIVE, TransferStatus.SUCCESS,
                            )
                        },
                        onFailure = { error ->
                            _state.value = ReceiveState.Error(error.message ?: "Download failed")
                            return@launch
                        },
                    )
                }
                _state.value = ReceiveState.Complete(_receivedFiles.value)
            } catch (e: Exception) {
                _state.value = ReceiveState.Error("Download error: ${e.message}")
            }
        }
    }

    fun terminate() {
        downloadJob?.cancel()
        downloadJob = null
        client.currentCall?.cancel()
        client.currentCall = null
        currentConnection?.let { conn ->
            client.disconnect(conn)
        }
        currentConnection = null
        currentDownloadUrl = null
        isProcessing = false
        _receivedFiles.value = emptyList()
        _state.value = ReceiveState.Idle
        CurrentConnection.clear()
    }

    fun clearHistory() {
        _receivedFiles.value = emptyList()
        _state.value = ReceiveState.Idle
    }

    fun dismissError() {
        val s = _state.value
        if (s is ReceiveState.Error && _receivedFiles.value.isNotEmpty()) {
            _state.value = ReceiveState.Complete(_receivedFiles.value)
        } else {
            _state.value = ReceiveState.Idle
        }
    }

    fun reset() {
        downloadJob?.cancel()
        currentConnection = null
        currentDownloadUrl = null
        isProcessing = false
        _state.value = if (_receivedFiles.value.isNotEmpty()) {
            ReceiveState.Complete(_receivedFiles.value)
        } else {
            ReceiveState.Idle
        }
    }

    private fun onQrScanned(rawValue: String) {
        connectToUrl(rawValue)
    }

    private suspend fun downloadAndSave(file: SenderFile): Result<Pair<Long, Uri>> = withContext(Dispatchers.IO) {
        try {
            val target = SavePathManager.getSaveTarget(app, file.name)

            when (target) {
                is SaveTarget.FileTarget -> {
                    target.file.parentFile?.mkdirs()
                    val output = try {
                        FileOutputStream(target.file)
                    } catch (e: Exception) {
                        return@withContext Result.failure(
                            Exception("Cannot write to save location (${e.message}). Switch to app-private storage in Settings.")
                        )
                    }
                    val result = client.downloadFile(file.downloadUrl, output) { read, total ->
                        updateProgress(read, total)
                        true
                    }
                    result.map { Pair(target.file.length(), safeFileUri(target.file)) }
                }
                is SaveTarget.MediaStoreTarget -> {
                    val resolver = app.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, target.contentValues)
                        ?: return@withContext Result.failure(
                            Exception("Could not create file in public Downloads. Switch to app-private in Settings.")
                        )

                    val outputStream = resolver.openOutputStream(uri)
                    if (outputStream == null) {
                        resolver.delete(uri, null, null)
                        return@withContext Result.failure(
                            Exception("Permission denied writing to public Downloads. Switch to app-private in Settings.")
                        )
                    }

                    outputStream.use { output ->
                        val downloadResult = client.downloadFile(file.downloadUrl, output) { read, total ->
                            updateProgress(read, total)
                            true
                        }
                        if (downloadResult.isFailure) {
                            resolver.delete(uri, null, null)
                            return@withContext downloadResult.map { Pair(0L, uri) }
                        }
                    }

                    val updateValues = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
                    resolver.update(uri, updateValues, null, null)

                    val cursor = resolver.query(uri, arrayOf(MediaStore.Downloads.SIZE), null, null, null)
                    val size = cursor?.use { if (it.moveToFirst()) it.getLong(0) else 0L } ?: 0L
                    cursor?.close()

                    Result.success(Pair(size, uri))
                }
                is SaveTarget.DocumentTreeTarget -> {
                    val resolver = app.contentResolver
                    val documentFile = DocumentFile.fromTreeUri(app, target.treeUri)
                        ?: return@withContext Result.failure(
                            Exception("Cannot access selected folder. Pick a new folder in Receive settings.")
                        )

                    val existing = documentFile.findFile(target.fileName)
                    if (existing != null) existing.delete()

                    val nameNoExt = target.fileName.substringBeforeLast('.')
                    val created = documentFile.createFile(target.mimeType, nameNoExt)
                        ?: return@withContext Result.failure(
                            Exception("Cannot create file in selected folder. Pick a new folder in Receive settings.")
                        )

                    val outputStream = resolver.openOutputStream(created.uri)
                        ?: return@withContext Result.failure(
                            Exception("Cannot write to selected folder. Pick a new folder in Receive settings.")
                        )

                    outputStream.use { output ->
                        val downloadResult = client.downloadFile(file.downloadUrl, output) { read, total ->
                            updateProgress(read, total)
                            true
                        }
                        if (downloadResult.isFailure) {
                            created.delete()
                            return@withContext downloadResult.map { Pair(0L, created.uri) }
                        }
                    }

                    val cursor = resolver.query(created.uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (sizeIndex >= 0) cursor.getLong(sizeIndex) else 0L
                        } else 0L
                    } ?: 0L

                    Result.success(Pair(cursor, created.uri))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun updateProgress(bytesReceived: Long, totalBytes: Long) {
        val s = _state.value
        if (s is ReceiveState.FileList) {
            _state.value = s.copy(bytesReceived = bytesReceived, totalBytes = totalBytes)
        }
    }

    private fun safeFileUri(file: File): Uri {
        return try {
            FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
        } catch (_: Exception) {
            android.net.Uri.fromFile(file)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
