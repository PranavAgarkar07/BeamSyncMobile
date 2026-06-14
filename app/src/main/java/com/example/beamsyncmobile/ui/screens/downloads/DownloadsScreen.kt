package com.example.beamsyncmobile.ui.screens.downloads

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.runtime.Composable
import com.example.beamsyncmobile.ui.screens.home.EmptyStateScreen

@Composable
fun DownloadsScreen() {
    EmptyStateScreen(
        icon = Icons.Default.CloudDownload,
        title = "No Downloads Yet",
        subtitle = "Scan a BeamSync QR code\nto receive files from your desktop",
    )
}
