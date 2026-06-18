package com.example.beamsyncmobile.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.network.SavePathManager
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing
import com.example.beamsyncmobile.ui.theme.ThemeManager
import com.example.beamsyncmobile.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToAbout: () -> Unit,
) {
    val context = LocalContext.current
    var transferMode by remember { mutableStateOf(1) }
    var autoReconnect by remember { mutableStateOf(true) }
    var connectionTimeoutIndex by remember { mutableStateOf(1) }
    var autoAcceptTimeoutIndex by remember { mutableStateOf(0) }
    var notifyComplete by remember { mutableStateOf(true) }
    var notifyDisconnect by remember { mutableStateOf(true) }
    var showSaveLocationDialog by remember { mutableStateOf(false) }
    var saveLocationVersion by remember { mutableIntStateOf(0) }
    val saveLocationMode = remember(saveLocationVersion) { SavePathManager.getMode(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = BeamsyncSpacing.space6),
    ) {
        Spacer(Modifier.height(BeamsyncSpacing.space6))

        Text(
            text = "SETTINGS",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        // ── CONNECTION ──
        SettingsSectionHeader(
            icon = Icons.Default.Tune,
            title = "CONNECTION",
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        SettingsCard {
            SettingsSectionLabel("Transfer Mode")
            Spacer(Modifier.height(BeamsyncSpacing.space2))
            ChipGroup(
                options = listOf("Accept All", "Ask First", "Block All"),
                selectedIndex = transferMode,
                onSelectionChange = { transferMode = it },
            )

            Spacer(Modifier.height(BeamsyncSpacing.space3))

            SettingsDivider()

            Spacer(Modifier.height(BeamsyncSpacing.space3))

            SettingsToggle(
                icon = Icons.Default.Sync,
                title = "Auto-Reconnect",
                subtitle = "Automatically reconnect when connection is lost",
                checked = autoReconnect,
                onCheckedChange = { autoReconnect = it },
            )

            Spacer(Modifier.height(BeamsyncSpacing.space3))
            SettingsDivider()
            Spacer(Modifier.height(BeamsyncSpacing.space3))

            SettingsChipRow(
                icon = Icons.Default.Timer,
                title = "Connection Timeout",
                subtitle = "Max time to establish initial connection",
                options = listOf("10s", "30s", "60s"),
                selectedIndex = connectionTimeoutIndex,
                onSelectionChange = { connectionTimeoutIndex = it },
            )

            if (transferMode == 1) {
                Spacer(Modifier.height(BeamsyncSpacing.space3))
                SettingsDivider()
                Spacer(Modifier.height(BeamsyncSpacing.space3))

                SettingsChipRow(
                    icon = Icons.Default.Timer,
                    title = "Auto-Accept Timeout",
                    subtitle = "How long to wait before auto-accepting a transfer",
                    options = listOf("15s", "30s", "60s"),
                    selectedIndex = autoAcceptTimeoutIndex,
                    onSelectionChange = { autoAcceptTimeoutIndex = it },
                )
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        // ── STORAGE ──
        SettingsSectionHeader(
            icon = Icons.Default.Folder,
            title = "STORAGE",
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        SettingsCard {
            SettingsClickableRow(
                icon = Icons.Default.Folder,
                title = "Save Location",
                subtitle = SavePathManager.getModeLabel(context, saveLocationMode),
                onClick = { showSaveLocationDialog = true },
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        // ── APPEARANCE ──
        SettingsSectionHeader(
            icon = Icons.Default.Palette,
            title = "APPEARANCE",
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        SettingsCard {
            SettingsSectionLabel("Theme")
            Spacer(Modifier.height(BeamsyncSpacing.space2))
            ChipGroup(
                options = ThemeMode.entries.map { it.name },
                selectedIndex = ThemeMode.entries.indexOf(ThemeManager.mode),
                onSelectionChange = { ThemeManager.changeMode(ThemeMode.entries[it]) },
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        // ── NOTIFICATIONS ──
        SettingsSectionHeader(
            icon = Icons.Default.Notifications,
            title = "NOTIFICATIONS",
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        SettingsCard {
            SettingsToggle(
                icon = Icons.Default.Notifications,
                title = "Transfer Complete",
                subtitle = "Notify when a file transfer finishes",
                checked = notifyComplete,
                onCheckedChange = { notifyComplete = it },
            )

            Spacer(Modifier.height(BeamsyncSpacing.space3))
            SettingsDivider()
            Spacer(Modifier.height(BeamsyncSpacing.space3))

            SettingsToggle(
                icon = Icons.Default.Wifi,
                title = "Connection Lost",
                subtitle = "Alert when the connection drops unexpectedly",
                checked = notifyDisconnect,
                onCheckedChange = { notifyDisconnect = it },
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        // ── INFO ──
        SettingsSectionHeader(
            icon = Icons.Default.Info,
            title = "INFO",
        )

        Spacer(Modifier.height(BeamsyncSpacing.space3))

        SettingsCard {
            SettingsClickableRow(
                icon = Icons.Default.Info,
                title = "About BeamSync",
                subtitle = "Version 1.0.0",
                onClick = onNavigateToAbout,
            )
        }

        Spacer(Modifier.height(BeamsyncSpacing.space10))
    }

    if (showSaveLocationDialog) {
        val currentCustomUri = SavePathManager.getCustomPathUri(context)
        val recentPaths = remember { SavePathManager.getRecentCustomPaths(context) }
        val currentMode = saveLocationMode

        AlertDialog(
            onDismissRequest = { showSaveLocationDialog = false },
            title = {
                Text(
                    text = "Save Location",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    SavePathManager.modes.forEach { mode ->
                        val label = SavePathManager.getModeLabel(context, mode)
                        val isSelected = currentMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SavePathManager.setMode(context, mode)
                                    saveLocationVersion++
                                    showSaveLocationDialog = false
                                }
                                .clip(MaterialTheme.shapes.small)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .padding(BeamsyncSpacing.space4),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.onPrimary),
        )
    }
}
                            Spacer(Modifier.width(BeamsyncSpacing.space3))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                        Spacer(Modifier.height(BeamsyncSpacing.space2))
                    }

                    if (recentPaths.isNotEmpty()) {
                        Spacer(Modifier.height(BeamsyncSpacing.space4))
                        Text(
                            text = "Recent locations",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = BeamsyncSpacing.space2),
                        )
                        Spacer(Modifier.height(BeamsyncSpacing.space2))
                        recentPaths.forEach { uri ->
                            val isSelected = currentMode == SavePathManager.PATH_CUSTOM && currentCustomUri == uri
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        SavePathManager.setCustomPathUri(context, uri)
                                        SavePathManager.setMode(context, SavePathManager.PATH_CUSTOM)
                                        saveLocationVersion++
                                        showSaveLocationDialog = false
                                    }
                                    .clip(MaterialTheme.shapes.small)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = BeamsyncSpacing.space4, vertical = BeamsyncSpacing.space2),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(BeamsyncSpacing.space3))
                                Text(
                                    text = SavePathManager.getCustomPathLabel(uri),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                            Spacer(Modifier.height(BeamsyncSpacing.space2))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSaveLocationDialog = false }) {
                    Text("DONE")
                }
            },
        )
    }
}

// ── Reusable Settings Components ──

@Composable
private fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.size(BeamsyncSpacing.space2))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.shapes.medium,
            )
            .padding(BeamsyncSpacing.space4),
    ) {
        content()
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun ChipGroup(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BeamsyncSpacing.space2),
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                    )
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.small,
                    )
                    .clickable { onSelectionChange(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option.uppercase(),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.size(BeamsyncSpacing.space3))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.size(BeamsyncSpacing.space2))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        )
    }
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.size(BeamsyncSpacing.space3))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun SettingsChipRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(BeamsyncSpacing.space3))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(BeamsyncSpacing.space2))
        ChipGroup(
            options = options,
            selectedIndex = selectedIndex,
            onSelectionChange = onSelectionChange,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(onNavigateToAbout = {})
}
