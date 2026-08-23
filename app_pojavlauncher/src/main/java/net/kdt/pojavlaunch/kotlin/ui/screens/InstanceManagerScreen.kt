package net.kdt.pojavlaunch.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.GetValue
import androidx.compose.runtime.LiveData
import androidx.compose.runtime.MutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.kdt.pojavlaunch.fragments.InstanceEditorFragment
import net.kdt.pojavlaunch.instances.Instances
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.kotlin.ui.viewmodel.DirectoryManagerViewModel
import java.io.File

@Composable
fun InstanceManagerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCloneConfirm by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var selectedInstance by remember { mutableStateOf<Instances.DisplayInstance?>(null) }

    val instances: Instances = remember {
        try { Instances.loadDisplay() } catch (e: Exception) { Instances.loadDisplay() }
    }

    // Select an instance to manage
    LaunchedEffect(instances.list) {
        if (instances.list.isNotEmpty()) {
            selectedInstance = instances.list.firstOrNull()
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
            color = androidx.compose.ui.graphics.Color(0xff24242c),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Instance Manager",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color(0xffffffff)
                )

                // Add button
                Surface(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(androidx.compose.ui.graphics.Color(0xffdc2626)),
                    tonalElevation = 2.dp
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Add,
                        contentDescription = "Add Instance",
                        tint = androidx.compose.ui.graphics.Color(0xff1a1a1a),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Instance list
        if (instances.list.isEmpty()) {
            Text(
                text = "No instances found. Create a new instance to get started.",
                color = androidx.compose.ui.graphics.Color(0xffaaaaaa),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
            ) {
                items(instances.list, key = { it.mInstanceRoot?.path ?: "" }) { instance ->
                    InstanceCard(
                        instance = instance,
                        onToggle = { isEnabled ->
                            // Toggle instance enabled/disabled
                        },
                        onSelect = {
                            selectedInstance = instance
                        },
                        onDelete = {
                            showDeleteConfirm = true
                        },
                        onClone = {
                            showCloneConfirm = true
                        }
                    )
                }
            }
        }

        // Create new instance dialog
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("New Instance") },
                text = {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Instance Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        try {
                            val newInstance = Instances.createInstance({ it ->
                                it.name = inputName
                                it.versionId = "1.12.2"
                                it.sharedData = true
                            }, inputName)
                            selectedInstance = newInstance
                            showCreateDialog = false
                            inputName = ""
                        } catch (e: Exception) {
                            Tools.showErrorRemote("Error creating instance", e)
                        }
                    }) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Delete confirmation
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete '${selectedInstance?.name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                Instances.removeInstance(selectedInstance!!)
                                selectedInstance = instances.list.firstOrNull()
                                showDeleteConfirm = false
                            } catch (e: Exception) {
                                Tools.showErrorRemote("Error deleting instance", e)
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
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

        // Clone confirmation
        if (showCloneConfirm) {
            AlertDialog(
                onDismissRequest = { showCloneConfirm = false },
                title = { Text("Confirm Clone") },
                text = { Text("Are you sure you want to clone '${selectedInstance?.name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                val cloned = Instances.createInstance({ it ->
                                    it.name = "${selectedInstance?.name} Copy"
                                    it.versionId = selectedInstance?.versionId
                                    it.sharedData = selectedInstance?.sharedData ?: true
                                }, "${selectedInstance?.name}-clone")
                                selectedInstance = cloned
                                showCloneConfirm = false
                            } catch (e: Exception) {
                                Tools.showErrorRemote("Error cloning instance", e)
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) { Text("Clone") }
                },
                dismissButton = {
                    TextButton(onClick = { showCloneConfirm = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun InstanceCard(
    instance: Instances.DisplayInstance,
    onToggle: (Boolean) -> Unit,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onClone: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = androidx.compose.ui.graphics.Color(0xff24242c)
        ),
        elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            // Instance info
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = instance.name ?: "Unnamed Instance",
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color(0xffffffff)
                )

                Text(
                    text = instance.versionId ?: "1.12.2",
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = androidx.compose.ui.graphics.Color(0xff888888)
                )
            }

            // Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onSelect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xff3a3a3a),
                        contentColor = androidx.compose.ui.graphics.Color(0xffffffff)
                    )
                ) { Text("Select") }

                Button(
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Delete") }

                Button(
                    onClick = onClone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) { Text("Clone") }
            }
        }
    }
}
