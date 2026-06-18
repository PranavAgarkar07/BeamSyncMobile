package com.example.beamsyncmobile.ui.navigation

import android.app.Activity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.beamsyncmobile.network.BeamSyncClient
import com.example.beamsyncmobile.network.CurrentConnection
import com.example.beamsyncmobile.ui.screens.downloads.DownloadsScreen
import com.example.beamsyncmobile.ui.screens.downloads.ReceiveViewModel
import com.example.beamsyncmobile.ui.screens.history.HistoryScreen
import com.example.beamsyncmobile.ui.screens.home.NewHomeScreen
import com.example.beamsyncmobile.ui.screens.scan.QrScannerScreen
import com.example.beamsyncmobile.ui.screens.scan.QrScannerViewModel
import com.example.beamsyncmobile.ui.screens.settings.AboutScreen
import com.example.beamsyncmobile.ui.screens.settings.SettingsScreen
import com.example.beamsyncmobile.ui.screens.startup.PermissionsScreen
import com.example.beamsyncmobile.ui.screens.uploads.UploadsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BeamsyncNavGraph() {
    val view = LocalView.current
    val bgArgb = MaterialTheme.colorScheme.background.toArgb()
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        window.statusBarColor = bgArgb
        val r = (bgArgb shr 16) and 0xFF
        val g = (bgArgb shr 8) and 0xFF
        val b = bgArgb and 0xFF
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
            (0.299 * r + 0.587 * g + 0.114 * b) > 128
    }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Permissions,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable<Permissions>(
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(0)) },
        ) {
            PermissionsScreen(
                onComplete = {
                    navController.navigate(Home) {
                        popUpTo<Permissions> { inclusive = true }
                    }
                },
            )
        }

        composable<Home>(
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(0)) },
        ) {
            NewHomeScreen(
                onNavigateToSettings = { navController.navigate(Settings) },
                onNavigateToHistory = { navController.navigate(History) },
                onNavigateToAbout = { navController.navigate(About) },
                onReceiveScanQr = { navController.navigate(QrScanner(mode = "receive")) },
                onReceiveManualUrl = { navController.navigate(QrScanner(mode = "receive")) },
                onSendScanQr = { navController.navigate(QrScanner(mode = "send")) },
                onSendManualUrl = { navController.navigate(QrScanner(mode = "send")) },
                onConnectFromUrl = { url, mode ->
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            BeamSyncClient().connectToSender(url)
                        }
                        result.onSuccess { connection ->
                            CurrentConnection.set(connection)
                            if (mode == "receive") {
                                navController.navigate(Downloads) {
                                    popUpTo<Home>()
                                }
                            } else {
                                navController.navigate(Uploads) {
                                    popUpTo<Home>()
                                }
                            }
                        }
                    }
                },
            )
        }

        composable<QrScanner>(
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(0)) },
        ) { backStackEntry ->
            val qrScanner: QrScanner = backStackEntry.toRoute()
            val scannerViewModel: QrScannerViewModel = viewModel()

            QrScannerScreen(
                viewModel = scannerViewModel,
                onConnected = { connection ->
                    CurrentConnection.set(connection)
                    if (qrScanner.mode == "receive") {
                        navController.navigate(Downloads) {
                            popUpTo<Home>()
                        }
                    } else {
                        navController.navigate(Uploads) {
                            popUpTo<Home>()
                        }
                    }
                },
            )
        }

        composable<Downloads>(
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(0)) },
        ) {
            val viewModel: ReceiveViewModel = viewModel()
            LaunchedEffect(Unit) {
                val conn = CurrentConnection.connection
                if (conn != null) {
                    val url = "${conn.scheme}://${conn.host}:${conn.port}"
                    viewModel.connectToUrl(url)
                }
            }
            DownloadsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable<Uploads>(
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(0)) },
        ) {
            UploadsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable<Settings>(
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(0)) },
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(About) },
            )
        }

        composable<About>(
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(0)) },
        ) {
            AboutScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable<History>(
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(0)) },
        ) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
