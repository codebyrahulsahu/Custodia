package com.example.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FamilyMemberProfile
import com.example.data.RelationshipType
import com.example.ui.theme.TrustTeal

val AVATAR_COLOR_PALETTE = listOf(
    0xFF0D9488, // Teal
    0xFF8B5CF6, // Purple
    0xFFF59E0B, // Amber
    0xFF06B6D4, // Cyan
    0xFFEC4899, // Pink
    0xFF3B82F6, // Blue
    0xFF10B981  // Emerald
)

val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-", "Unknown")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMemberModal(
    memberToEdit: FamilyMemberProfile? = null,
    onDismiss: () -> Unit,
    onSave: (
        id: String?,
        name: String,
        relationship: RelationshipType,
        dob: String,
        bloodGroup: String,
        phone: String,
        email: String,
        avatarColorHex: Long
    ) -> Unit
) {
    var name by remember { mutableStateOf(memberToEdit?.name ?: "") }
    var relationship by remember { mutableStateOf(memberToEdit?.relationship ?: RelationshipType.HEAD) }
    var dob by remember { mutableStateOf(memberToEdit?.dob ?: "01 Jan 1995") }
    var bloodGroup by remember { mutableStateOf(memberToEdit?.bloodGroup ?: "B+") }
    var phone by remember { mutableStateOf(memberToEdit?.phone ?: "") }
    var email by remember { mutableStateOf(memberToEdit?.email ?: "") }
    var selectedColor by remember { mutableStateOf(memberToEdit?.avatarColorHex ?: AVATAR_COLOR_PALETTE[0]) }

    var relationshipExpanded by remember { mutableStateOf(false) }
    var bloodGroupExpanded by remember { mutableStateOf(false) }

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Modal Header
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
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TrustTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (memberToEdit == null) "Add Family Member" else "Edit Family Member",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f))
                    }
                }

                // Avatar Color Selection & Initials Preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Preview
                    val initials = name.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()
                        .ifBlank { "FM" }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(selectedColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    // Colors Palette
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AVATAR_COLOR_PALETTE.forEach { colorHex ->
                            val isSelected = selectedColor == colorHex
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = colorHex }
                            )
                        }
                    }
                }

                // Full Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g. Rajesh Sharma") },
                    singleLine = true,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_member_name")
                )

                // Relationship Dropdown
                ExposedDropdownMenuBox(
                    expanded = relationshipExpanded,
                    onExpandedChange = { relationshipExpanded = !relationshipExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = relationship.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Relationship *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationshipExpanded) },
                        colors = custodiaTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = relationshipExpanded,
                        onDismissRequest = { relationshipExpanded = false },
                        modifier = Modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        RelationshipType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    relationship = type
                                    relationshipExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date of Birth & Blood Group Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Date of Birth") },
                        placeholder = { Text("DD Mon YYYY") },
                        singleLine = true,
                        colors = custodiaTextFieldColors(),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("input_member_dob")
                    )

                    ExposedDropdownMenuBox(
                        expanded = bloodGroupExpanded,
                        onExpandedChange = { bloodGroupExpanded = !bloodGroupExpanded },
                        modifier = Modifier.weight(0.9f)
                    ) {
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = { bloodGroup = it },
                            label = { Text("Blood") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodGroupExpanded) },
                            colors = custodiaTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = bloodGroupExpanded,
                            onDismissRequest = { bloodGroupExpanded = false },
                            modifier = Modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            BLOOD_GROUPS.forEach { bg ->
                                DropdownMenuItem(
                                    text = { Text(bg, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        bloodGroup = bg
                                        bloodGroupExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Phone & Email Fields
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (Optional)") },
                    placeholder = { Text("+91 98765 43210") },
                    singleLine = true,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address (Optional)") },
                    placeholder = { Text("name@example.com") },
                    singleLine = true,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Actions: Cancel & Save
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
                            if (name.isNotBlank()) {
                                onSave(
                                    memberToEdit?.id,
                                    name.trim(),
                                    relationship,
                                    dob.trim(),
                                    bloodGroup.trim(),
                                    phone.trim(),
                                    email.trim(),
                                    selectedColor
                                )
                            }
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_save_member")
                    ) {
                        Text(
                            text = if (memberToEdit == null) "Add Member" else "Save Changes",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun custodiaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TrustTeal,
    unfocusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.outline,
    focusedLabelColor = TrustTeal,
    unfocusedLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
    cursorColor = TrustTeal,
    focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
)
