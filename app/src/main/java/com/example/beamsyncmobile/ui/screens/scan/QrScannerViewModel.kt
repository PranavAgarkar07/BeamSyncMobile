package com.example.beamsyncmobile.ui.screens.scan

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.beamsyncmobile.network.BeamSyncClient
import com.example.beamsyncmobile.network.ServerConnection
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

sealed class ScannerState {
    data object Waiting : ScannerState()
    data object Scanning : ScannerState()
    data object Connecting : ScannerState()
    data class Found(val connection: ServerConnection) : ScannerState()
    data class Error(val message: String) : ScannerState()
    data object Denied : ScannerState()
}

data class RecentConnection(
    val label: String,
    val scheme: String,
    val host: String,
    val port: Int,
    val token: String,
) {
    fun toServerConnection(): ServerConnection = ServerConnection(scheme, host, port, token)
}

class QrScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val client = BeamSyncClient()
    private val prefs = application.getSharedPreferences("beamsync_prefs", Context.MODE_PRIVATE)

    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Waiting)
    val scannerState: StateFlow<ScannerState> = _scannerState.asStateFlow()

    private val _manualUrl = MutableStateFlow("")
    val manualUrl: StateFlow<String> = _manualUrl.asStateFlow()

    private val _manualError = MutableStateFlow<String?>(null)
    val manualError: StateFlow<String?> = _manualError.asStateFlow()

    private val _torchEnabled = MutableStateFlow(false)
    val torchEnabled: StateFlow<Boolean> = _torchEnabled.asStateFlow()

    private val _connectEvent = Channel<ServerConnection>(Channel.BUFFERED)
    val connectEvent: Flow<ServerConnection> = _connectEvent.receiveAsFlow()

    private val _recentConnections = MutableStateFlow<List<RecentConnection>>(emptyList())
    val recentConnections: StateFlow<List<RecentConnection>> = _recentConnections.asStateFlow()

    private var isProcessing = false

    init {
        loadRecentConnections()
    }

    fun toggleTorch() {
        _torchEnabled.value = !_torchEnabled.value
    }

    fun onCameraReady() {
        _scannerState.value = ScannerState.Scanning
    }

    fun onPermissionDenied() {
        _scannerState.value = ScannerState.Denied
    }

    fun reset() {
        _scannerState.value = ScannerState.Waiting
        _manualUrl.value = ""
        _manualError.value = null
        isProcessing = false
    }

    fun onManualUrlChanged(url: String) {
        _manualUrl.value = url
        _manualError.value = null
    }

    fun connectManual() {
        val url = _manualUrl.value.trim()
        if (url.isBlank()) {
            _manualError.value = "Enter a URL"
            return
        }
        connectToUrl(url)
    }

    fun connectToRecent(rc: RecentConnection) {
        val url = "${rc.scheme}://${rc.host}:${rc.port}/?token=${rc.token}"
        connectToUrl(url)
    }

    fun clearRecentConnections() {
        _recentConnections.value = emptyList()
        prefs.edit().remove("recent_connections").apply()
    }

    fun scanFromBitmap(bitmap: Bitmap) {
        if (isProcessing) return
        isProcessing = true
        _scannerState.value = ScannerState.Scanning

        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val scanner = BarcodeScanning.getClient()
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        resolveUrl(rawValue)
                        return@addOnSuccessListener
                    }
                }
                _scannerState.value = ScannerState.Error("No QR code found in image")
                isProcessing = false
            }
            .addOnFailureListener { e ->
                _scannerState.value = ScannerState.Error("Failed to scan image: ${e.message}")
                isProcessing = false
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
        val scanner = BarcodeScanning.getClient()

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        Log.d("QrScanner", "QR scanned: $rawValue")
                        isProcessing = true
                        resolveUrl(rawValue)
                        break
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun connectToUrl(url: String) {
        _scannerState.value = ScannerState.Connecting
        viewModelScope.launch {
            val result = client.connect(url)
            result.fold(
                onSuccess = { connection ->
                    saveRecentConnection(connection)
                    _connectEvent.send(connection)
                },
                onFailure = { error ->
                    val msg = when {
                        error.message?.contains("token", ignoreCase = true) == true ->
                            "BeamSync Desktop is not running or the QR code has expired. Open BeamSync Desktop and try again."
                        error.message?.contains("timeout", ignoreCase = true) == true ->
                            "Connection timed out. Make sure both devices are on the same Wi-Fi network."
                        error.message?.contains("refused", ignoreCase = true) == true ||
                        error.message?.contains("connect", ignoreCase = true) == true ->
                            "Could not reach this address. Check that the IP and port are correct."
                        else -> error.message ?: "Connection failed. Please try again."
                    }
                    _scannerState.value = ScannerState.Error(msg)
                },
            )
        }
    }

    private fun resolveUrl(rawUrl: String) {
        connectToUrl(rawUrl)
    }

    private fun saveRecentConnection(connection: ServerConnection) {
        val label = "${connection.host}:${connection.port}"
        val entry = RecentConnection(
            label = label,
            scheme = connection.scheme,
            host = connection.host,
            port = connection.port,
            token = connection.token,
        )
        val updated = _recentConnections.value
            .filter { it.host != connection.host || it.port != connection.port }
            .toMutableList()
        updated.add(0, entry)
        val kept = updated.take(5)
        _recentConnections.value = kept

        val json = JSONArray()
        for (rc in kept) {
            json.put(JSONObject().apply {
                put("label", rc.label)
                put("scheme", rc.scheme)
                put("host", rc.host)
                put("port", rc.port)
                put("token", rc.token)
            })
        }
        prefs.edit().putString("recent_connections", json.toString()).apply()
    }

    private fun loadRecentConnections() {
        val raw = prefs.getString("recent_connections", null) ?: return
        try {
            val json = JSONArray(raw)
            val list = mutableListOf<RecentConnection>()
            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)
                list.add(
                    RecentConnection(
                        label = obj.getString("label"),
                        scheme = obj.getString("scheme"),
                        host = obj.getString("host"),
                        port = obj.getInt("port"),
                        token = obj.getString("token"),
                    )
                )
            }
            _recentConnections.value = list
        } catch (_: Exception) { }
    }
}
