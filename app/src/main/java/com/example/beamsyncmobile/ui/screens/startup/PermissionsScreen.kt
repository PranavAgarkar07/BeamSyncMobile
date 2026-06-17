package com.example.beamsyncmobile.ui.screens.startup

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiStatusbar4Bar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class WifiStatus(
    val isConnected: Boolean = false,
    val frequencyMHz: Int? = null,
    val isHotspot: Boolean = false,
)

private fun getWifiStatus(context: Context): WifiStatus {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return WifiStatus()
    val caps = cm.getNetworkCapabilities(network) ?: return WifiStatus()
    if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return WifiStatus()

    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val freq = wifiManager.connectionInfo.frequency
    val noInternet = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    return WifiStatus(isConnected = true, frequencyMHz = freq, isHotspot = noInternet)
}

private fun frequencyLabel(freq: Int): String = when {
    freq < 5000 -> "2.4 GHz"
    freq < 60000 -> "5 GHz"
    else -> "6 GHz"
}

@Composable
fun PermissionsScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cameraGranted by remember { mutableStateOf(false) }
    var storageGranted by remember { mutableStateOf(false) }
    var wifiStatus by remember { mutableStateOf(WifiStatus()) }
    var checkingWifi by remember { mutableStateOf(true) }
    var allDone by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> cameraGranted = granted }

    val storageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> storageGranted = granted }

    // WRITE_EXTERNAL_STORAGE is a no-op on API 30+ (Android 11+).
    // On API 29 it's meaningful because we have requestLegacyExternalStorage="true".
    // On API 28- it's required for public Downloads access.
    val needsWriteExternalStorage = Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q

    LaunchedEffect(Unit) {
        cameraLauncher.launch(Manifest.permission.CAMERA)
        if (needsWriteExternalStorage) {
            storageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            storageGranted = true
        }
        wifiStatus = withContext(Dispatchers.IO) { getWifiStatus(context) }
        checkingWifi = false
    }

    fun refreshWifi() {
        checkingWifi = true
        scope.launch {
            wifiStatus = withContext(Dispatchers.IO) { getWifiStatus(context) }
            checkingWifi = false
        }
    }

    val freqText = wifiStatus.frequencyMHz?.let { frequencyLabel(it) }
    val hasIssue = wifiStatus.isConnected && wifiStatus.isHotspot && (wifiStatus.frequencyMHz ?: 9999) < 5000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = BeamsyncSpacing.space8)
            .padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "SETUP",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 3.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Ready to Sync",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space8))

        // Camera Permission
        StatusCard(
            icon = Icons.Default.CameraAlt,
            title = "Camera",
            subtitle = if (cameraGranted) "Granted" else "Required for QR scanning",
            isOk = cameraGranted,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space3))

        // Storage Permission
        StatusCard(
            icon = Icons.Default.Folder,
            title = "Storage",
            subtitle = when {
                !needsWriteExternalStorage -> "Not required on Android 11+"
                storageGranted -> "Access ready"
                else -> "Grant storage access"
            },
            isOk = storageGranted,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space3))

        // WiFi Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BeamsyncSpacing.space2))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(BeamsyncSpacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (checkingWifi) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Icon(
                    imageVector = if (wifiStatus.isConnected) Icons.Default.SignalWifiStatusbar4Bar else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (wifiStatus.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(BeamsyncSpacing.space3))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "WiFi / Hotspot",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = when {
                        checkingWifi -> "Detecting..."
                        !wifiStatus.isConnected -> "Not connected. Join the same network as your desktop."
                        wifiStatus.isHotspot -> "Hotspot active"
                        else -> "WiFi connected"
                    } + if (freqText != null) " \u00B7 $freqText" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { refreshWifi() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh WiFi status")
            }
        }

        // Warning for 2.4GHz hotspot
        AnimatedVisibility(visible = hasIssue, enter = fadeIn(), exit = fadeOut()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = BeamsyncSpacing.space3)
                    .clip(RoundedCornerShape(BeamsyncSpacing.space2))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(BeamsyncSpacing.space4),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(BeamsyncSpacing.space2))
                Text(
                    text = "Your hotspot is on 2.4 GHz. For faster transfers, switch to 5 GHz in your hotspot settings.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space8))

        Button(
            onClick = { onComplete() },
            modifier = Modifier
                .fillMaxWidth()
                .height(BeamsyncSpacing.space12),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
            enabled = cameraGranted,
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        if (!cameraGranted) {
            Spacer(Modifier.height(BeamsyncSpacing.space2))
            Text(
                text = "Camera access is required to scan QR codes",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StatusCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isOk: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BeamsyncSpacing.space2))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(BeamsyncSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isOk) Icons.Default.CheckCircle else icon,
            contentDescription = null,
            tint = if (isOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(BeamsyncSpacing.space3))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
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
