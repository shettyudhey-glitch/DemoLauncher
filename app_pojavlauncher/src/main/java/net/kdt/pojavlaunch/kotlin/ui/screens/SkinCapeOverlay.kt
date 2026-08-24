package net.kdt.pojavlaunch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.kdt.pojavlaunch.authenticator.accounts.Accounts
import net.kdt.pojavlaunch.authenticator.accounts.MinecraftAccount
import net.kdt.pojavlaunch.skin.AndroidSkinAnalyzer
import net.kdt.pojavlaunch.skin.LocalUuidUtils
import net.kdt.pojavlaunch.skin.SkinModelType
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

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
    var skinName by remember { mutableStateOf("") }
    var capeName by remember { mutableStateOf("") }
    var modelType by remember { mutableStateOf(SkinModelType.STEVE) }

    val skinLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = copyUriToInternal(context, it, "skin")
            if (file != null) {
                val bytes = file.readBytes()
                if (analyzer.validate(bytes)) {
                    selectedSkinFile = file
                    skinName = file.nameWithoutExtension
                    modelType = analyzer.detectModel(bytes)
                } else {
                    android.widget.Toast.makeText(context, "Invalid skin dimensions!", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val capeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = copyUriToInternal(context, it, "cape")
            if (file != null) {
                selectedCapeFile = file
                capeName = file.nameWithoutExtension
            }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Skin preview section
                Text(
                    text = "Skin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xffffffff)
                )

                OutlinedTextField(
                    value = skinName,
                    onValueChange = { skinName = it },
                    label = { Text("Skin File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { skinLauncher.launch("image/*") },
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
                    value = capeName,
                    onValueChange = { capeName = it },
                    label = { Text("Cape File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { capeLauncher.launch("image/*") },
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
                        try {
                            val skinBytes = selectedSkinFile?.readBytes()
                            val capeBytes = selectedCapeFile?.readBytes()
                            if (skinBytes != null && analyzer.validate(skinBytes)) {
                                if (analyzer.prepareSkin(skinBytes) == null) {
                                    android.widget.Toast.makeText(context, "Invalid skin dimensions!", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                            }
                            val account: MinecraftAccount? = Accounts.getCurrent()
                            if (account != null) {
                                account.skinPath = selectedSkinFile?.absolutePath
                                account.capePath = selectedCapeFile?.absolutePath
                                account.skinModel = modelType
                                account.save()
                            }
                            android.widget.Toast.makeText(context, "Skin & Cape saved", android.widget.Toast.LENGTH_SHORT).show()
                            onBack()
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Error saving: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
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

private fun copyUriToInternal(context: android.content.Context, uri: android.net.Uri, prefix: String): File? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val fileName = "${prefix}_${UUID.randomUUID()}.png"
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        }
    } catch (e: Exception) {
        null
    }
}
