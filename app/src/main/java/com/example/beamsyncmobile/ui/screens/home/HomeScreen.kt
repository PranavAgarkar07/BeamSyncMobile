package com.example.beamsyncmobile.ui.screens.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.beamsyncmobile.R
import com.example.beamsyncmobile.network.ServerConnection
import com.example.beamsyncmobile.ui.components.BeamsyncButton
import com.example.beamsyncmobile.ui.components.BeamsyncButtonSize
import com.example.beamsyncmobile.ui.components.BeamsyncButtonVariant
import com.example.beamsyncmobile.ui.screens.scan.QrScannerScreen
import com.example.beamsyncmobile.ui.screens.scan.QrScannerViewModel
import com.example.beamsyncmobile.ui.screens.scan.ScannerState
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

    when {
        !cameraPermissionGranted && scannerState !is ScannerState.Denied -> {
            M3PermissionPrompt(
                icon = Icons.Default.QrCodeScanner,
                title = stringResource(R.string.camera_access_required),
                subtitle = stringResource(R.string.camera_access_description),
                actionLabel = stringResource(R.string.grant_camera_access),
                onAction = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            )
        }

        scannerState is ScannerState.Denied -> {
            M3PermissionDenied(
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

@Composable
private fun M3PermissionPrompt(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BeamsyncSpacing.space8)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space10))

        BeamsyncButton(
            text = actionLabel,
            variant = BeamsyncButtonVariant.Primary,
            size = BeamsyncButtonSize.Large,
            fullWidth = true,
            onClick = onAction,
        )
    }
}

@Composable
private fun M3PermissionDenied(
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BeamsyncSpacing.space8)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.VideocamOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp),
        )

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Text(
            text = stringResource(R.string.camera_permission_denied),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Text(
            text = stringResource(R.string.camera_permission_denied_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space10))

        BeamsyncButton(
            text = stringResource(R.string.open_settings),
            variant = BeamsyncButtonVariant.Secondary,
            size = BeamsyncButtonSize.Large,
            fullWidth = true,
            onClick = onOpenSettings,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = BeamsyncSpacing.space2),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        BeamsyncButton(
            text = stringResource(R.string.try_again),
            variant = BeamsyncButtonVariant.Ghost,
            size = BeamsyncButtonSize.Default,
            fullWidth = true,
            onClick = onRetry,
        )
    }
}
