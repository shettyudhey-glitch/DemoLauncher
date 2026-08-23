package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.hilt.navigation.compose.hiltViewModel
import net.kdt.pojavlaunch.ui.theme.PojavTheme
import net.kdt.pojavlaunch.kotlin.ui.viewmodel.DirectoryManagerViewModel
import java.io.File

@Composable
fun FileManagerFragment(
    onBack: () -> Unit,
    onNavigate: (Int) -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var selectedTab by remember { mutableStateOf("/mods") }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    val toggleStates = remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

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

    val entries: List<File> = viewModel.entries

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        // [ + Add ] Crimson accent button — route to Content Downloader
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(
                onClick = { onNavigate(2) },
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDC2626))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add content",
                    tint = Color(0xFF08080A),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Directory tabs
        val tabData = listOf(
            "/mods" to "Mods",
            "/saves" to "Saves",
            "/resourcepacks" to "Resource Packs",
            "/shaderpacks" to "Shader Packs",
            "/logs" to "Logs"
        )
        TabRow(
            selectedTabIndex = tabData.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xff24242c))
        ) {
            tabData.forEach { (tabKey, tabLabel) ->
                Tab(
                    text = { Text(tabLabel) },
                    selected = selectedTab == tabKey,
                    onClick = { selectedTab = tabKey }
                )
            }
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xff555555))
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
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(entries, key = { it.hashCode() }) { file ->
                    val isToggled = toggleStates.value[file.path] ?: false
                    FileRow(
                        file = file,
                        isSelected = selectedFile?.path == file.path,
                        toggleState = isToggled,
                        onToggle = { isSelected ->
                            val newMap = toggleStates.value.toMutableMap()
                            newMap[file.path] = isSelected
                            toggleStates.value = newMap
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
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
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
                    TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancel") }
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
                    TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) Color(0xff3a3a3a) else Color(0xff24242c)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // File info
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xff888888)
                    )
                } else {
                    Text(
                        text = formatFileSize(file.length()),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xff888888)
                    )
                }
            }

            // Toggle switch
            Switch(
                checked = toggleState,
                onCheckedChange = onToggle
            )
        }
    }
}

private fun formatFileSize(size: Long): String {
    val kb = 1024f
    val mb = kb * 1024f
    return if (size >= mb) {
        "%.1f MB".format(size / mb)
    } else if (size >= kb) {
        "%.1f KB".format(size / kb)
    } else {
        "${size} bytes"
    }
}
