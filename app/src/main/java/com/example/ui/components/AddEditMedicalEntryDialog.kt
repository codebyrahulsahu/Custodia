package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalServices
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FamilyMemberProfile
import com.example.data.MedicalEntry
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrustTeal
import com.example.ui.theme.VaultCardBorder
import com.example.ui.theme.VaultNavyDark
import com.example.ui.theme.VaultSurface

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
        attachedReportName: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf(entryToEdit?.title ?: "") }
    var date by remember { mutableStateOf(entryToEdit?.date ?: "28 Aug 2026") }
    var doctorOrClinic by remember { mutableStateOf(entryToEdit?.doctorOrClinic ?: "") }
    var notes by remember { mutableStateOf(entryToEdit?.notes ?: "") }
    var attachedReportName by remember { mutableStateOf(entryToEdit?.attachedReportName ?: "Lab_Report.pdf") }
    var hasAttachment by remember { mutableStateOf(entryToEdit?.attachedReportName != null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, VaultCardBorder, RoundedCornerShape(16.dp)),
            color = VaultNavyDark
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
                                color = TextPrimary
                            )
                            Text(
                                text = "Patient: ${selectedMember.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
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

                // Attached Report File
                OutlinedTextField(
                    value = attachedReportName,
                    onValueChange = { attachedReportName = it },
                    label = { Text("Attached Lab / Prescription File (Optional)") },
                    placeholder = { Text("e.g. Blood_Test_Report_Aug2026.pdf") },
                    singleLine = true,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

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
                        colors = ButtonDefaults.buttonColors(containerColor = VaultSurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextSecondary)
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
                                    if (attachedReportName.isNotBlank()) attachedReportName.trim() else null
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
