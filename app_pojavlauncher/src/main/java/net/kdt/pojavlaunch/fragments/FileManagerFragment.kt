package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.ComposeView
import androidx.compose.foundation.Background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.relativesize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.shape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isFocusable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.size
import androidx.compose.foundation.indicator.rememberToggleSource
import androidx.compose.foundation.indicator.Toggleable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.preferSize
import androidx.compose.ui.layout.ContentScale
import kotlin.result.success
import net.kdt.pojavlaunch.BaseActivity
import net.kdt.pojavlaunch.authenticator.Accounts
import net.kdt.pojavlaunch.kotlin.ui.viewmodel.DirectoryManagerViewModel
import net.kdt.pojavlaunch.ui.screens.SettingsOverlay
import net.kdt.pojavlaunch.ui.screens.AboutOverlay
import net.kdt.pojavlaunch.ui.screens.ModManagerFragment
import dagger.hilt.android.lifecycle.hiltViewModel

/**
 * File Manager Fragment - displays directory structure with toolbar actions
 * and directory quick tabs for /mods, /saves, /resourcepacks, /shaderpacks, /logs
 */
@Composable
fun FileManagerFragment(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var selectedTab by remember { mutableStateOf("/mods") }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var showSelectAll by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var toggleStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    val viewModel: DirectoryManagerViewModel = hiltViewModel()
    viewModel.init("Files", null)

    // Refresh when tab changes
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            "/mods" -> viewModel.init("Mods", "/mods")
            "/saves" -> viewModel.init("Saves", "/saves")
            "/resourcepacks" -> viewModel.init("Resourcepacks", "/resourcepacks")
            "/shaderpacks" -> viewModel.init("Shaderpacks", "/shaderpacks")
            "/logs" -> viewModel.init("Logs", "/logs")
        }
    }

    val entries by viewModel.entries

    // Toolbar actions
    val isDeleteEnabled = selectedFile != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Start,
        scrollState = rememberScrollState()
    ) {
        // Directory tabs
        TabRow(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xff24242c),
            selectionIndicator = {
                Box(
                    modifier = Modifier.height(2.dp).offset(top = 4.dp),
                    color = Color(0xffdc2626)
                )
            }
        ) {
            Tab(
                text = { Text("Mods") },
                selected = selectedTab == "/mods",
                onSelect = { selectedTab = "/mods" }
            )
            Tab(
                text = { Text("Saves") },
                selected = selectedTab == "/saves",
                onSelect = { selectedTab = "/saves" }
            )
            Tab(
                text = { Text("Resource Packs") },
                selected = selectedTab == "/resourcepacks",
                onSelect = { selectedTab = "/resourcepacks" }
            )
            Tab(
                text = { Text("Shader Packs") },
                selected = selectedTab == "/shaderpacks",
                onSelect = { selectedTab = "/shaderpacks" }
            )
            Tab(
                text = { Text("Logs") },
                selected = selectedTab == "/logs",
                onSelect = { selectedTab = "/logs" }
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xff555555)),
            contentAlignment = Alignment.CenterStart
        )

        // Entries list
        if (entries.isEmpty()) {
            Text(
                text = "No files found in selected directory.",
                color = Color(0xffaaaaaa),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(Float.PositiveInfinity),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(4.dp),
                scrollState = rememberScrollState()
            ) {
                items(entries, key = { it.path }) { file ->
                    FileRow(
                        file = file,
                        isSelected = selectedFile?.path == file.path,
                        toggleState = toggleStates[file.path] ?: false,
                        onToggle = { isSelected ->
                            val newState = if (isSelected) toggleStates + (file.path to true) else toggleStates + (file.path to false)
                            toggleStates = newState
                        },
                        onSelect = {
                            selectedFile = if (selectedFile?.path == file.path) null else file
                        },
                        onDelete = {
                            showDeleteConfirm = true
                        },
                        onRename = {
                            inputName = file.name
                            showRenameDialog = true
                        }
                    )
                }
            }
        }

        // Delete confirmation
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete '${selectedFile?.name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSelected()
                            showDeleteConfirm = false
                            selectedFile = null
                            toggleStates = toggleStates + (selectedFile?.path ?: "" to false)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) { Text("Delete") }
                },
                dismissButton = {
                    @Suppress("DEPRECATION")
                    TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(id = android.R.string.cancel)) }
                }
            )
        }

        // New folder dialog
        if (showNewFolderDialog) {
            AlertDialog(
                onDismissRequest = { showNewFolderDialog = false },
                title = { Text("New Folder") },
                text = {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Folder Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.createFolder(inputName)
                        showNewFolderDialog = false
                        inputName = ""
                    }) { Text("Create") }
                },
                dismissButton = {
                    @Suppress("DEPRECATION")
                    TextButton(onClick = { showNewFolderDialog = false }) { Text(stringResource(id = android.R.string.cancel)) }
                }
            )
        }

        // Rename dialog
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename") },
                text = {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("New Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.renameSelected(inputName)
                        showRenameDialog = false
                        inputName = ""
                    }) { Text("Rename") }
                },
                dismissButton = {
                    @Suppress("DEPRECATION")
                    TextButton(onClick = { showRenameDialog = false }) { Text(stringResource(id = android.R.string.cancel)) }
                }
            )
        }
    }
}

/**
 * File row with selection toggle
 */
@Composable
fun FileRow(
    file: File,
    isSelected: Boolean,
    toggleState: Boolean,
    onToggle: (Boolean) -> Unit,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) Color(0xff3a3a3a) else Color(0xff24242c)
        ),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // File info
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = file.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xffffffff)
                )

                if (file.isDirectory) {
                    Text(
                        text = "${file.listFiles()?.size ?: 0} items",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.caption,
                        color = Color(0xff888888)
                    )
                } else {
                    Text(
                        text = formatFileSize(file.length()),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.caption,
                        color = Color(0xff888888)
                    )
                }
            }

            // Toggle switch for selection
            androidx.compose.material3.ToggleSwitch(
                checked = toggleState,
                onCheckedChange = onToggle,
                colors = androidx.compose.material3.ToggleSwitchDefaults.colors(
                    thumbColor = Color(0xff1a1a1a),
                    trackColor = if (toggleState) Color(0xffdc2626) else Color(0xff555555)
                )
            )

            // Selection indicator
            Box(
                modifier = Modifier.size(20.dp).padding(end = 4.dp),
                color = if (isSelected) Color(0xffdc2626) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
        }
    }

    /**
     * Format file size to human readable
     */
    private fun formatFileSize(size: Long): String {
        val kb = 1024f
        val mb = kb * 1024f

        return if (size >= mb) {
            "${(size / mb).toStringAsFixed(1)} MB"
        } else if (size >= kb) {
            "${(size / kb).toStringAsFixed(1)} KB"
        } else {
            "${size} bytes"
        }
    }
}

@Preview
@Composable
private fun FileManagerPreview() {
    PojavTheme(dynamicColor = true) {
        FileManagerFragment(onBack = {})
    }
}