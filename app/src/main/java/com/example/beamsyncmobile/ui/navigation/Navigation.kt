package com.example.beamsyncmobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Scan       : Screen("scan",       "SCAN",    Icons.Default.QrCodeScanner)
    data object Downloads  : Screen("downloads",  "RECEIVE", Icons.Default.CloudDownload)
    data object Uploads    : Screen("uploads",    "SEND",    Icons.Default.CloudUpload)
    data object Settings   : Screen("settings",   "SETTINGS",Icons.Default.Settings)
    data object Connection : Screen(
        "connection/{scheme}/{host}/{port}/{token}",
        "CONNECTED",
        Icons.Default.QrCodeScanner,
    )
}

data class BottomNavItem(val screen: Screen, val label: String)

object Screens {
    val items = listOf(
        BottomNavItem(Screen.Scan, "SCAN"),
        BottomNavItem(Screen.Downloads, "RECEIVE"),
        BottomNavItem(Screen.Uploads, "SEND"),
        BottomNavItem(Screen.Settings, "SETTINGS"),
    )
}
