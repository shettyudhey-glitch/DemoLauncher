package net.kdt.pojavlaunch.ui.screens

import androidx.compose.foundation.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.kdt.pojavlaunch.skin.AndroidSkinAnalyzer
import net.kdt.pojavlaunch.skin.LocalUuidUtils
import net.kdt.pojavlaunch.skin.SkinManager
import net.kdt.pojavlaunch.skin.SkinModelType
import kotlin.io.path.AbsolutePath
import java.io.File

@Composable
fun SkinCapeOverlay(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val analyzer = AndroidSkinAnalyzer

    var showSkinDialog by remember { mutableStateOf(false) }
    var showCapeDialog by remember { mutableStateOf(false) }
    var selectedSkinFile by remember { mutableStateOf<File?>(null) }
    var selectedCapeFile by remember { mutableStateOf<File?>(null) }
    var inputName by remember { mutableStateOf("") }
    var modelType by remember { mutableStateOf(SkinModelType.STEVE) }

    // Skin selection
    LaunchedEffect({}, showSkinDialog) {
        if (showSkinDialog) {
            // Handle skin selection
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
                    text = "Skin & Cape",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xffffffff)
                )

                // Add skin button
                Surface(
                    onClick = { showSkinDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xffdc2626)),
                    tonalElevation = 2.dp
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Add,
                        contentDescription = "Add Skin",
                        tint = Color(0xff1a1a1a),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Skin/Cape content
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            color = Color(0xff1a1a1a),
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBy(16.dp)
            ) {
                // Skin preview section
                Text(
                    text = "Skin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xffffffff)
                )

                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Skin File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        // Handle skin selection
                        showSkinDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xff3a3a3a),
                        contentColor = Color(0xffffffff)
                    )
                ) {
                    Text("Select Skin File")
                }

                // Cape preview section
                Text(
                    text = "Cape",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xffffffff)
                )

                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Cape File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        // Handle cape selection
                        showCapeDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xff3a3a3a),
                        contentColor = Color(0xffffffff)
                    )
                ) {
                    Text("Select Cape File")
                }

                // Model type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Model:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xff888888)
                    )

                    Button(
                        onClick = { modelType = SkinModelType.STEVE },
                        modifier = Modifier.size(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (modelType == SkinModelType.STEVE) Color(0xffdc2626) else Color(0xff24242c),
                            contentColor = Color(0xffffffff)
                        )
                    ) {
                        Text("Steve", style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = { modelType = SkinModelType.ALEX },
                        modifier = Modifier.size(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (modelType == SkinModelType.ALEX) Color(0xffdc2626) else Color(0xff24242c),
                            contentColor = Color(0xffffffff)
                        )
                    ) {
                        Text("Alex", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Save button
                Button(
                    onClick = {
                        // Save skin/cape configuration
                        showSkinDialog = false
                        showCapeDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xffdc2626),
                        contentColor = Color(0xff1a1a1a)
                    )
                ) {
                    Text("Save Skin & Cape", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
