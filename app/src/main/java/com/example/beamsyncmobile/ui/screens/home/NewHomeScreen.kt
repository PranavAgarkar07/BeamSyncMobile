package com.example.beamsyncmobile.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_onboarding_logo),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(40.dp),
                            )
                            Spacer(Modifier.width(BeamsyncSpacing.space3))
                            Column {
                                Text(
                                    text = "BeamSync",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "File Transfer",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(BeamsyncSpacing.space4))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = BeamsyncSpacing.space6),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    Spacer(Modifier.height(BeamsyncSpacing.space2))

                    DrawerItem(
                        icon = Icons.Default.History,
                        label = "History",
                        subtitle = "View past transfers",
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToHistory()
                        },
                    )
                    DrawerItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        subtitle = "Preferences & storage",
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToSettings()
                        },
                    )
                    DrawerItem(
                        icon = Icons.Default.Info,
                        label = "About",
                        subtitle = "Version & credits",
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToAbout()
                        },
                    )

                    Spacer(Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = BeamsyncSpacing.space6)
                            .padding(bottom = BeamsyncSpacing.space6),
                    ) {
                        Text(
                            text = "BeamSync v1.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background,
                        ),
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = BeamsyncSpacing.space6),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }

                Spacer(Modifier.height(BeamsyncSpacing.space2))

                Icon(
                    painter = painterResource(R.drawable.ic_onboarding_logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(120.dp),
                )

                Spacer(Modifier.height(BeamsyncSpacing.space3))

                Text(
                    text = "BeamSync",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = "Wireless File Transfers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(BeamsyncSpacing.space8))

                ActionCard(
                    icon = Icons.Default.CloudDownload,
                    label = "Receive",
                    subtitle = "Download files from desktop",
                    containerColor = MaterialTheme.colorScheme.primary,
                    onContainerColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { showReceiveSheet = true },
                )
                Spacer(Modifier.height(BeamsyncSpacing.space3))

                ActionCard(
                    icon = Icons.Default.CloudUpload,
                    label = "Send",
                    subtitle = "Upload files to desktop",
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    onContainerColor = MaterialTheme.colorScheme.onTertiary,
                    onClick = { showSendSheet = true },
                )

                Spacer(Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = BeamsyncSpacing.space4),
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(BeamsyncSpacing.space1))
                    Text(
                        text = "Both devices must be on the same network",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                }
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
private fun ActionCard(
    icon: ImageVector,
    label: String,
    subtitle: String,
    containerColor: Color,
    onContainerColor: Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BeamsyncSpacing.space4),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = BeamsyncSpacing.space6),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = onContainerColor,
                    modifier = Modifier.size(52.dp),
                )
            }
            Spacer(Modifier.height(BeamsyncSpacing.space3))
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = BeamsyncSpacing.space6, vertical = BeamsyncSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(BeamsyncSpacing.space3))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
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
