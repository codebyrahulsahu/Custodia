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
import androidx.compose.material.icons.filled.HealthAndSafety
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
import com.example.ui.theme.TrustTeal

@Composable
fun EditBaselineMedicalModal(
    member: FamilyMemberProfile,
    onDismiss: () -> Unit,
    onSave: (
        memberId: String,
        bloodGroup: String,
        allergies: String,
        chronicConditions: String,
        currentMedications: String,
        pastIllnesses: String,
        doctorNotes: String
    ) -> Unit
) {
    var bloodGroup by remember { mutableStateOf(member.bloodGroup) }
    var allergies by remember { mutableStateOf(member.allergies) }
    var chronicConditions by remember { mutableStateOf(member.chronicConditions) }
    var currentMedications by remember { mutableStateOf(member.currentMedications) }
    var pastIllnesses by remember { mutableStateOf(member.pastIllnessesOrSurgeries) }
    var doctorNotes by remember { mutableStateOf(member.doctorNotes) }

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
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Baseline Medical Profile",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "For ${member.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f))
                    }
                }

                // Blood Group & Allergies
                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = { bloodGroup = it },
                    label = { Text("Blood Group") },
                    placeholder = { Text("e.g. B+, O+, AB-") },
                    singleLine = true,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Known Allergies (Food / Drug / Environmental)") },
                    placeholder = { Text("e.g. Penicillin, Peanuts, Dust mites, None") },
                    singleLine = true,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Chronic Conditions & Current Medications
                OutlinedTextField(
                    value = chronicConditions,
                    onValueChange = { chronicConditions = it },
                    label = { Text("Chronic Conditions") },
                    placeholder = { Text("e.g. Hypertension, Diabetes, Thyroid, None") },
                    singleLine = true,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = currentMedications,
                    onValueChange = { currentMedications = it },
                    label = { Text("Current Daily Medications") },
                    placeholder = { Text("e.g. Telmisartan 40mg, Metformin 500mg") },
                    singleLine = true,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Past Illnesses / Surgeries & Doctor's Notes
                OutlinedTextField(
                    value = pastIllnesses,
                    onValueChange = { pastIllnesses = it },
                    label = { Text("Past Illnesses / Surgeries / Procedures") },
                    placeholder = { Text("e.g. Appendectomy (2018), Knee Arthroscopy") },
                    singleLine = true,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = doctorNotes,
                    onValueChange = { doctorNotes = it },
                    label = { Text("Doctor's Key Advice / Notes") },
                    placeholder = { Text("e.g. Regular 6-month lipid profile checkup") },
                    maxLines = 3,
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
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            onSave(
                                member.id,
                                bloodGroup.trim().ifBlank { "Unknown" },
                                allergies.trim().ifBlank { "None known" },
                                chronicConditions.trim().ifBlank { "None" },
                                currentMedications.trim().ifBlank { "None" },
                                pastIllnesses.trim().ifBlank { "None" },
                                doctorNotes.trim()
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_save_baseline_medical")
                    ) {
                        Text("Save Health Profile", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
