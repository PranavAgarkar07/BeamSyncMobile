package com.example.beamsyncmobile.ui.screens.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.beamsyncmobile.network.ServerConnection
import com.example.beamsyncmobile.ui.components.BeamsyncButton
import com.example.beamsyncmobile.ui.components.BeamsyncButtonSize
import com.example.beamsyncmobile.ui.components.BeamsyncButtonVariant
import com.example.beamsyncmobile.ui.screens.scan.QrScannerScreen
import com.example.beamsyncmobile.ui.screens.scan.QrScannerViewModel
import com.example.beamsyncmobile.ui.screens.scan.ScannerState
import com.example.beamsyncmobile.ui.theme.BeamsyncColors
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

@Composable
fun HomeScreen(
    onConnected: (ServerConnection) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: QrScannerViewModel = viewModel()
    val scannerState by viewModel.scannerState.collectAsState()

    val cameraPermissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.onCameraReady()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        val recheck = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        )
        if (recheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.onCameraReady()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(cameraPermissionGranted) {
        if (cameraPermissionGranted && scannerState is ScannerState.Waiting) {
            viewModel.onCameraReady()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BeamsyncColors.surfaceBase),
    ) {
        when {
            !cameraPermissionGranted && scannerState !is ScannerState.Denied -> {
                PermissionPrompt(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                )
            }

            scannerState is ScannerState.Denied -> {
                PermissionDeniedScreen(
                    onOpenSettings = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        )
                        settingsLauncher.launch(intent)
                    },
                    onRetry = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                )
            }

            else -> {
                QrScannerScreen(
                    viewModel = viewModel,
                    onConnected = onConnected,
                )
            }
        }
    }
}

@Composable
private fun PermissionPrompt(
    onRequestPermission: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(BeamsyncSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "CAMERA ACCESS REQUIRED",
            color = BeamsyncColors.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space3))
        Text(
            text = "BeamSync needs camera access\nto scan desktop QR codes",
            color = BeamsyncColors.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space8))
        BeamsyncButton(
            text = "GRANT CAMERA ACCESS",
            variant = BeamsyncButtonVariant.Primary,
            size = BeamsyncButtonSize.Large,
            fullWidth = true,
            onClick = onRequestPermission,
        )
    }
}

@Composable
private fun PermissionDeniedScreen(
    onOpenSettings: () -> Unit,
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
            text = "CAMERA PERMISSION DENIED",
            color = BeamsyncColors.surfaceCritical,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space3))
        Text(
            text = "Camera access is permanently blocked.\nEnable it in Settings to scan QR codes.",
            color = BeamsyncColors.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space8))
        BeamsyncButton(
            text = "OPEN SETTINGS",
            variant = BeamsyncButtonVariant.Secondary,
            size = BeamsyncButtonSize.Large,
            fullWidth = true,
            onClick = onOpenSettings,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space3))
        BeamsyncButton(
            text = "TRY AGAIN",
            variant = BeamsyncButtonVariant.Ghost,
            size = BeamsyncButtonSize.Default,
            fullWidth = true,
            onClick = onRetry,
        )
    }
}
