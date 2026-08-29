package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FamilyMemberProfile
import com.example.data.FileStorageHelper
import com.example.data.MedicalEntry
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TrustTeal
import com.example.ui.theme.VerifiedGreen
import java.io.File

@Composable
fun AddEditMedicalEntryDialog(
    selectedMember: FamilyMemberProfile,
    entryToEdit: MedicalEntry? = null,
    onDismiss: () -> Unit,
    onSave: (
        id: String?,
        memberId: String,
        date: String,
        title: String,
        doctorOrClinic: String,
        notes: String,
        attachedReportName: String?,
        attachedReportPath: String?
    ) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(entryToEdit?.title ?: "") }
    var date by remember { mutableStateOf(entryToEdit?.date ?: "28 Aug 2026") }
    var doctorOrClinic by remember { mutableStateOf(entryToEdit?.doctorOrClinic ?: "") }
    var notes by remember { mutableStateOf(entryToEdit?.notes ?: "") }
    var attachedReportName by remember { mutableStateOf(entryToEdit?.attachedReportName ?: "") }
    var attachedReportPath by remember { mutableStateOf(entryToEdit?.attachedReportPath) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }

    // File manager launcher for lab reports
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedInfo = FileStorageHelper.saveUriToVault(
                context = context,
                sourceUri = uri,
                targetFolder = FileStorageHelper.getMedicalReportsDir(context)
            )
            if (savedInfo != null) {
                attachedReportPath = savedInfo.filePath
                attachedReportName = savedInfo.fileName
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val capturedFile = tempCameraFile
        if (success && capturedFile != null && capturedFile.exists()) {
            val destFolder = FileStorageHelper.getMedicalReportsDir(context)
            val destFile = File(destFolder, "med_camera_${System.currentTimeMillis()}.jpg")
            capturedFile.copyTo(destFile, overwrite = true)

            attachedReportPath = destFile.absolutePath
            attachedReportName = destFile.name
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(TrustTeal.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (entryToEdit == null) "Add Medical Consultation" else "Edit Medical Record",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Patient: ${selectedMember.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f))
                    }
                }

                // Consultation Title / Reason
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Consultation Title / Purpose *") },
                    placeholder = { Text("e.g. Annual Health Checkup, Cardiology Review") },
                    singleLine = true,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_medical_title")
                )

                // Date & Doctor / Clinic
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date *") },
                        placeholder = { Text("DD Mon YYYY") },
                        singleLine = true,
                        colors = custodiaTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = doctorOrClinic,
                        onValueChange = { doctorOrClinic = it },
                        label = { Text("Doctor / Clinic *") },
                        placeholder = { Text("e.g. Dr. S. K. Mehta") },
                        singleLine = true,
                        colors = custodiaTextFieldColors(),
                        modifier = Modifier.weight(1.3f)
                    )
                }

                // Clinical Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Clinical Notes / Diagnosis / Advice *") },
                    placeholder = { Text("e.g. Blood pressure normal, continue prescribed diet and medication...") },
                    minLines = 3,
                    maxLines = 5,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_medical_notes")
                )

                // Attached Report File Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                        .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ATTACH PRESCRIPTION / LAB REPORT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    filePickerLauncher.launch("*/*")
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("File Manager", fontSize = 11.5.sp, color = ElectricCyan)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val (uri, file) = FileStorageHelper.createTempCameraUri(context)
                                        tempCameraFile = file
                                        cameraLauncher.launch(uri)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Take Photo", fontSize = 11.5.sp, color = TrustTeal)
                            }
                        }

                        if (attachedReportName.isNotBlank() || attachedReportPath != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = attachedReportName.ifBlank { "Attached_Report" },
                                        fontSize = 11.5.sp,
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }

                                Row {
                                    if (attachedReportPath != null) {
                                        IconButton(
                                            onClick = { FileStorageHelper.openFile(context, attachedReportPath!!) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.OpenInNew, contentDescription = "Open", tint = ElectricCyan, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            attachedReportPath = null
                                            attachedReportName = ""
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() && doctorOrClinic.isNotBlank()) {
                                onSave(
                                    entryToEdit?.id,
                                    selectedMember.id,
                                    date.trim().ifBlank { "28 Aug 2026" },
                                    title.trim(),
                                    doctorOrClinic.trim(),
                                    notes.trim().ifBlank { "Consultation completed." },
                                    if (attachedReportName.isNotBlank()) attachedReportName.trim() else null,
                                    attachedReportPath
                                )
                            }
                        },
                        enabled = title.isNotBlank() && doctorOrClinic.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_save_medical_entry")
                    ) {
                        Text(
                            text = if (entryToEdit == null) "Save Record" else "Update Record",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
