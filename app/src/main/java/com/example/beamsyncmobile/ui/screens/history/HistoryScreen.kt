package com.example.beamsyncmobile.ui.screens.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.beamsyncmobile.data.history.HistoryRepository
import com.example.beamsyncmobile.data.history.TransferDirection
import com.example.beamsyncmobile.data.history.TransferRecord
import com.example.beamsyncmobile.data.history.TransferStatus
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class SortOrder { NEWEST_FIRST, OLDEST_FIRST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val tabs = listOf("Receive", "Send")

    var tabIndex by remember { mutableIntStateOf(0) }
    var filterStatus by remember { mutableStateOf<TransferStatus?>(null) }
    var sortOrder by remember { mutableStateOf(SortOrder.NEWEST_FIRST) }
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val records = remember { mutableStateListOf<TransferRecord>().also { it.addAll(HistoryRepository.getHistory(context)) } }
    val focusManager = LocalFocusManager.current

    val filtered by remember { derivedStateOf {
        records
            .filter { if (tabIndex == 0) it.direction == TransferDirection.RECEIVE else it.direction == TransferDirection.SEND }
            .filter { filterStatus == null || it.status == filterStatus }
            .filter { searchQuery.isBlank() || it.fileName.contains(searchQuery, ignoreCase = true) }
            .let { if (sortOrder == SortOrder.NEWEST_FIRST) it.sortedByDescending { r -> r.timestamp } else it.sortedBy { r -> r.timestamp } }
    }}

    fun deleteRecord(record: TransferRecord) {
        HistoryRepository.deleteRecord(context, record.id)
        records.removeAll { it.id == record.id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(if (sortOrder == SortOrder.NEWEST_FIRST) "Sort: Oldest first" else "Sort: Newest first") },
                                onClick = {
                                    sortOrder = if (sortOrder == SortOrder.NEWEST_FIRST) SortOrder.OLDEST_FIRST else SortOrder.NEWEST_FIRST
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null) },
                            )
                            DropdownMenuItem(
                                text = { Text("Clear all history") },
                                onClick = { showMenu = false; showClearDialog = true },
                                leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            PrimaryTabRow(
                selectedTabIndex = tabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title, fontWeight = if (tabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                    )
                }
            }

            // Filter chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = BeamsyncSpacing.space4, vertical = BeamsyncSpacing.space2),
                horizontalArrangement = Arrangement.spacedBy(BeamsyncSpacing.space2),
            ) {
                FilterChip(selected = filterStatus == null, onClick = { filterStatus = null }, label = { Text("All") }, colors = filterChipColors())
                FilterChip(selected = filterStatus == TransferStatus.SUCCESS, onClick = { filterStatus = TransferStatus.SUCCESS }, label = { Text("Successful") }, colors = filterChipColors())
                FilterChip(selected = filterStatus == TransferStatus.FAILED, onClick = { filterStatus = TransferStatus.FAILED }, label = { Text("Failed") }, colors = filterChipColors())
            }

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = BeamsyncSpacing.space4, vertical = BeamsyncSpacing.space1),
                placeholder = { Text("Search by filename...", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { searchQuery = ""; focusManager.clearFocus() }) { Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp)) } }
                } else null,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                shape = MaterialTheme.shapes.small,
            )

            HorizontalDivider()

            // Animated tab content
            AnimatedContent(
                targetState = tabIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "tabContent",
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(BeamsyncSpacing.space8),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp),
                            )
                            Spacer(Modifier.height(BeamsyncSpacing.space4))
                            Text(
                                text = when {
                                    searchQuery.isNotBlank() -> "No matches for \"$searchQuery\""
                                    filterStatus != null -> "No matching transfers"
                                    else -> "No transfer history yet"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = BeamsyncSpacing.space4),
                        verticalArrangement = Arrangement.spacedBy(BeamsyncSpacing.space2),
                    ) {
                        items(filtered, key = { it.id }) { record ->
                            HistoryItem(
                                record = record,
                                onDelete = { deleteRecord(record) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear history?") },
            text = { Text("This will remove all transfer history. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    HistoryRepository.clearHistory(context)
                    records.clear()
                    showClearDialog = false
                }) { Text("CLEAR", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("CANCEL") }
            },
        )
    }
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
)

@Composable
private fun HistoryItem(record: TransferRecord, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy \u00B7 h:mm a", Locale.getDefault()) }
    val isSuccess = record.status == TransferStatus.SUCCESS

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BeamsyncSpacing.space2))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = BeamsyncSpacing.space3, top = BeamsyncSpacing.space3, bottom = BeamsyncSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (record.direction == TransferDirection.RECEIVE) Icons.Default.CloudDownload else Icons.Default.CloudUpload,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.width(BeamsyncSpacing.space3))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.fileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                text = formatSize(record.fileSize) + " \u00B7 " + dateFormat.format(Date(record.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(BeamsyncSpacing.space1))
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes.toDouble() / (1024 * 1024))
    else -> "%.1f GB".format(bytes.toDouble() / (1024 * 1024 * 1024))
}
