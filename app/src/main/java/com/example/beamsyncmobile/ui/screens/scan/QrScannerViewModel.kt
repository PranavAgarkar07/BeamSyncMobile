package com.example.beamsyncmobile.ui.screens.scan

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beamsyncmobile.network.BeamSyncClient
import com.example.beamsyncmobile.network.ServerConnection
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScannerState {
    data object Waiting : ScannerState()
    data object Scanning : ScannerState()
    data class Found(val connection: ServerConnection) : ScannerState()
    data class Error(val message: String) : ScannerState()
    data object Denied : ScannerState()
}

class QrScannerViewModel : ViewModel() {

    private val client = BeamSyncClient()

    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Waiting)
    val scannerState: StateFlow<ScannerState> = _scannerState.asStateFlow()

    private val _manualUrl = MutableStateFlow("")
    val manualUrl: StateFlow<String> = _manualUrl.asStateFlow()

    private val _manualError = MutableStateFlow<String?>(null)
    val manualError: StateFlow<String?> = _manualError.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private var isProcessing = false

    fun onCameraReady() {
        _scannerState.value = ScannerState.Scanning
    }

    fun onPermissionDenied() {
        _scannerState.value = ScannerState.Denied
    }

    fun reset() {
        _scannerState.value = ScannerState.Waiting
        _manualError.value = null
        isProcessing = false
        _isConnecting.value = false
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
        resolveUrl(url)
    }

    fun consumeConnection(): ServerConnection? {
        val state = _scannerState.value
        if (state is ScannerState.Found) {
            _scannerState.value = ScannerState.Waiting
            return state.connection
        }
        return null
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

    /**
     * Resolve a raw URL into a ServerConnection.
     * Works for both sender and receiver mode URLs.
     */
    private fun resolveUrl(rawUrl: String) {
        viewModelScope.launch {
            _scannerState.value = ScannerState.Scanning

            val result = client.connect(rawUrl)
            result.fold(
                onSuccess = { connection ->
                    Log.d("QrScanner", "Connected: ${connection.host}:${connection.port}")
                    _scannerState.value = ScannerState.Found(connection)
                },
                onFailure = { error ->
                    Log.e("QrScanner", "Connection failed", error)
                    _scannerState.value = ScannerState.Error(
                        error.message ?: "Failed to connect to BeamSync"
                    )
                },
            )
        }
    }
}
