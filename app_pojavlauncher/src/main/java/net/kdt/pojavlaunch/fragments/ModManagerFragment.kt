package net.kdt.pojavlaunch.fragments
import java.io.File
import androidx.compose.ui.platform.ComposeView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel

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
import androidx.compose.ui.unit.intdp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.size
import androidx.compose.foundation.indicator.rememberToggleSource
import androidx.compose.foundation.indicator.Toggleable
import androidx.compose.foundation.indicator.ToggleGroup
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
import net.kdt.pojavlaunch.modloaders.modpacks.models.Constants
import net.kdt.pojavlaunch.ui.screens.AccountManagerOverlay
import net.kdt.pojavlaunch.ui.theme.PojavTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kdt.pojavlaunch.ui.screens.SettingsOverlay
import net.kdt.pojavlaunch.ui.screens.AboutOverlay

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

    // Refresh toggle states when entries change
    val entries by viewModel.entries

    LaunchedEffect(entries) {
        // Initialize toggle states for .jar files
        val jarEntries = entries.filter { it.name.endsWith(".jar") }
        if (toggleStates.isEmpty() && !jarEntries.isEmpty()) {
            toggleStates = remember { mutableMapOf(jarEntries.indices.map { it to false }.toMap()) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Start,
        scrollState = rememberScrollState()
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
                        .background(Color(0xffdc2626), RoundedCornerShape(18.dp)),
                    tonalElevation = 2.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Mod",
                        modifier = Modifier.size(20.dp).color(Color(0xff1a1a1a))
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
                modifier = Modifier.fillMaxSize(Float.PositiveInfinity),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp),
                scrollState = rememberScrollState()
            ) {
                items(entries, key = { it.path }) { file ->
                    ModCard(
                        file = file,
                        isToggled = toggleStates[file.path] ?: false,
                        onToggle = { isToggled ->
                            val newState = if (isToggled) toggleStates + (file.path to false) else toggleStates + (file.path to true)
                            toggleStates = newState
                            // Toggle mod enabled/disable
                            toggleMod(file, isToggled)
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
                text = { Text("Are you sure you want to delete '${viewModel.selectedFile?.name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSelected()
                            showDeleteConfirm = false
                            toggleStates = toggleStates - (viewModel.selectedFile?.path ?: "")
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
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xff24242c)
        ),
        elevation = 1.dp
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = file.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xffffffff)
                )

                Text(
                    text = file.path.substringAfter("/mods/"),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.caption,
                    color = Color(0xff888888)
                )
            }

            // Toggle switch
            androidx.compose.material3.ToggleSwitch(
                checked = isToggled,
                onCheckedChange = onToggle,
                colors = androidx.compose.material3.ToggleSwitchDefaults.colors(
                    thumbColor = Color(0xff1a1a1a),
                    trackColor = if (isToggled) Color(0xffdc2626) else Color(0xff555555)
                )
            )
        }
    }

    /**
     * Toggle mod enabled/disable without deleting the file
     */
    fun toggleMod(file: File, enabled: Boolean) {
        // Rename file to .enabled/.disabled or remove extension toggle
        if (enabled) {
            // Mark as enabled - could add .enabled extension or just track state
            // For now, just update the toggle state
        } else {
            // Mark as disabled - add .disabled extension
            val disabledFile = File(file.parentFile, "${file.name}.disabled")
            if (file.renameTo(disabledFile)) {
                // Successfully renamed
            }
        }
    }
}

@Preview
@Composable
private fun ModManagerPreview() {
    PojavTheme(dynamicColor = true) {
        ModManagerFragment(onBack = {})
    }
}