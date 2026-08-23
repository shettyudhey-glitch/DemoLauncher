package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
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
import androidx.hilt.navigation.compose.hiltViewModel
import net.kdt.pojavlaunch.ui.theme.PojavTheme
import net.kdt.pojavlaunch.kotlin.ui.viewmodel.DirectoryManagerViewModel
import java.io.File

/**
 * Mod Manager Fragment - displays installed .jar files in /mods directory
 * with steel grey (#24242C) cards and crimson toggle switches (#DC2626 active track)
 */
@Composable
fun ModManagerFragment(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var toggleStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    val viewModel: DirectoryManagerViewModel = hiltViewModel()
    viewModel.init("Mods", "/mods")

    val entries: List<File> = viewModel.entries

    LaunchedEffect(entries) {
        val jarEntries = entries.filter { it.name.endsWith(".jar") || it.name.endsWith(".jar.disabled") }
        if (jarEntries.isNotEmpty()) {
            val newStates = mutableMapOf(toggleStates.value)
            jarEntries.forEach { file ->
                if (!newStates.containsKey(file.path)) {
                    val enabled = !file.name.endsWith(".disabled")
                    newStates[file.path] = enabled
                }
            }
            toggleStates.value = newStates
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        // Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = Color(0xff24242c),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mod Manager",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xffffffff)
                )

                // Add button
                Surface(
                    onClick = { showNewFolderDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xffdc2626)),
                    tonalElevation = 2.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Mod",
                        tint = Color(0xff1a1a1a),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Mods list
        if (entries.isEmpty()) {
            Text(
                text = "No mods installed. Add mods to /mods directory.",
                color = Color(0xffaaaaaa),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(entries, key = { it.path }) { file ->
                    ModCard(
                        file = file,
                        isToggled = toggleStates[file.path] ?: false,
                        onToggle = { isEnabled ->
                            val newStates = mutableMapOf(toggleStates.value)
                            newStates[file.path] = isEnabled
                            toggleStates.update { it }
                            toggleMod(file, isEnabled)
                        }
                    )
                }
            }
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

        // Delete confirmation
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete '${viewModel.selectedFile?.name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSelected()
                            showDeleteConfirm = false
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
    }
}

/**
 * Mod Card with crimson toggle switch
 */
@Composable
fun ModCard(
    file: File,
    isToggled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xff24242c)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Mod info
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = file.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xffffffff)
                )

                Text(
                    text = if (file.isDirectory) {
                        "${file.listFiles()?.size ?: 0} items"
                    } else {
                        formatFileSize(file.length())
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xff888888)
                )
            }

            // Toggle switch
            Switch(
                checked = isToggled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xff1a1a1a),
                    checkedTrackColor = Color(0xffdc2626),
                    uncheckedThumbColor = Color(0xff1a1a1a),
                    uncheckedTrackColor = Color(0xff555555)
                )
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

fun toggleMod(file: File, enabled: Boolean) {
    val newName = if (enabled) {
        file.name.removeSuffix(".disabled")
    } else {
        "${file.name}.disabled"
    }
    if (newName.isEmpty() || newName == file.name) return

    val target = File(file.parentFile, newName)
    if (target.exists()) {
        return
    }
    file.renameTo(target)
}

/**
 * Fragment wrapper for Compose — retains the same pattern as FileManagerFragment
 */
class ModManagerFragment : androidx.fragment.app.Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(inflater.context).apply {
            setContent {
                PojavTheme {
                    ModManagerFragment(onBack = {})
                }
            }
        }
    }
}
