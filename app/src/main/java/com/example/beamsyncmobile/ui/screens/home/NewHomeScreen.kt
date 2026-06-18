package com.example.beamsyncmobile.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.beamsyncmobile.R
import com.example.beamsyncmobile.ui.components.ConnectionOptionsSheet
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing
import kotlinx.coroutines.launch

@Composable
fun NewHomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onReceiveScanQr: () -> Unit,
    onReceiveManualUrl: () -> Unit,
    onSendScanQr: () -> Unit,
    onSendManualUrl: () -> Unit,
    onConnectFromUrl: (url: String, mode: String) -> Unit = { _, _ -> },
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showReceiveSheet by remember { mutableStateOf(false) }
    var showSendSheet by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxHeight()) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(Modifier.height(BeamsyncSpacing.space8))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = BeamsyncSpacing.space6)
                            .padding(bottom = BeamsyncSpacing.space4),
                    ) {
                        Column {
                            Text(
                                text = "BeamSync",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "File Transfer",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(Modifier.height(BeamsyncSpacing.space4))

                    DrawerItem(
                        icon = Icons.Default.History,
                        label = "History",
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToHistory()
                        },
                    )
                    DrawerItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToSettings()
                        },
                    )
                    DrawerItem(
                        icon = Icons.Default.Info,
                        label = "About",
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToAbout()
                        },
                    )
                }
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = BeamsyncSpacing.space8),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(BeamsyncSpacing.space4))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(BeamsyncSpacing.space4))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_onboarding_logo),
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.Unspecified,
                        modifier = Modifier.size(72.dp),
                    )
                }

                Spacer(Modifier.height(BeamsyncSpacing.space4))

                Text(
                    text = "BeamSync",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Wireless File Transfers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(BeamsyncSpacing.space10))

                BigActionButton(
                    icon = Icons.Default.CloudDownload,
                    label = "Receive",
                    subtitle = "Download files from desktop",
                    color = MaterialTheme.colorScheme.primary,
                    trailingIcon = Icons.Default.CloudDownload,
                    onClick = { showReceiveSheet = true },
                )
                Spacer(Modifier.height(BeamsyncSpacing.space4))

                BigActionButton(
                    icon = Icons.Default.CloudUpload,
                    label = "Send",
                    subtitle = "Upload files to desktop",
                    color = MaterialTheme.colorScheme.secondary,
                    trailingIcon = Icons.Default.CloudUpload,
                    onClick = { showSendSheet = true },
                )
            }

            // Hamburger
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.padding(start = BeamsyncSpacing.space2),
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }

    val receiveGalleryUrl = remember(showReceiveSheet, onConnectFromUrl) {
        { url: String -> onConnectFromUrl(url, "receive") }
    }
    if (showReceiveSheet) {
        ConnectionOptionsSheet(
            mode = "receive",
            onDismiss = { showReceiveSheet = false },
            onScanQr = onReceiveScanQr,
            onManualUrl = onReceiveManualUrl,
            onGalleryUrl = receiveGalleryUrl,
        )
    }

    val sendGalleryUrl = remember(showSendSheet, onConnectFromUrl) {
        { url: String -> onConnectFromUrl(url, "send") }
    }
    if (showSendSheet) {
        ConnectionOptionsSheet(
            mode = "send",
            onDismiss = { showSendSheet = false },
            onScanQr = onSendScanQr,
            onManualUrl = onSendManualUrl,
            onGalleryUrl = sendGalleryUrl,
        )
    }
}

@Composable
private fun BigActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(BeamsyncSpacing.space3))
            .background(color.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = BeamsyncSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(BeamsyncSpacing.space2))
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.width(BeamsyncSpacing.space4))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = BeamsyncSpacing.space6, vertical = BeamsyncSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(BeamsyncSpacing.space3))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
