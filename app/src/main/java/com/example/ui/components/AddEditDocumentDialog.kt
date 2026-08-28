package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.DocumentItem
import com.example.data.FamilyMemberProfile
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrustTeal
import com.example.ui.theme.VaultCardBorder
import com.example.ui.theme.VaultNavyDark
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VerifiedGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

val POPULAR_DOC_TYPES = listOf(
    "Aadhaar Card",
    "PAN Card",
    "Passport",
    "Driving Licence",
    "Birth Certificate",
    "Degree / Marksheet",
    "Insurance Policy",
    "Bank Passbook"
)

@Composable
fun AddEditDocumentDialog(
    selectedMember: FamilyMemberProfile,
    documentToEdit: DocumentItem? = null,
    onDismiss: () -> Unit,
    onSave: (
        id: String?,
        memberId: String,
        title: String,
        documentType: String,
        documentNumber: String,
        issuer: String,
        issueDate: String,
        expiryDate: String?,
        notes: String,
        ocrExtracted: Boolean
    ) -> Unit
) {
    var documentType by remember { mutableStateOf(documentToEdit?.documentType ?: "Aadhaar Card") }
    var title by remember { mutableStateOf(documentToEdit?.title ?: "Aadhaar Card") }
    var documentNumber by remember { mutableStateOf(documentToEdit?.documentNumber ?: "") }
    var issuer by remember { mutableStateOf(documentToEdit?.issuer ?: "UIDAI, Govt of India") }
    var issueDate by remember { mutableStateOf(documentToEdit?.issueDate ?: "15 Jan 2020") }
    var isPermanent by remember { mutableStateOf(documentToEdit?.expiryDate == null) }
    var expiryDate by remember { mutableStateOf(documentToEdit?.expiryDate ?: "14 Jan 2030") }
    var notes by remember { mutableStateOf(documentToEdit?.notes ?: "") }
    var ocrExtracted by remember { mutableStateOf(documentToEdit?.ocrExtracted ?: false) }

    var isScanningOcr by remember { mutableStateOf(false) }
    var ocrSuccessMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

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
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = TrustTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (documentToEdit == null) "Add Document" else "Edit Document",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Member: ${selectedMember.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                // OCR Auto-Scan Banner & Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(VaultSurface)
                        .border(1.dp, if (ocrExtracted) VerifiedGreen.copy(alpha = 0.4f) else ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (ocrExtracted) Icons.Default.CheckCircle else Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = if (ocrExtracted) VerifiedGreen else ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (ocrExtracted) "OCR Extraction Complete" else "Smart OCR Auto-Scan",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (ocrExtracted) VerifiedGreen else TextPrimary
                                    )
                                    Text(
                                        text = if (ocrExtracted) "Fields extracted & ready for review" else "Auto-extract Document Number, Issuer & Dates",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isScanningOcr = true
                                        delay(1400) // Simulated optical character recognition parsing
                                        // Auto-extract sample data based on selected document type
                                        val sampleNumber = when {
                                            documentType.contains("Aadhaar", ignoreCase = true) -> "4829 " + (1000..9999).random() + " " + (1000..9999).random()
                                            documentType.contains("PAN", ignoreCase = true) -> "ABCPS" + (1000..9999).random() + "K"
                                            documentType.contains("Passport", ignoreCase = true) -> "Z" + (1000000..9999999).random()
                                            documentType.contains("Driving", ignoreCase = true) -> "DL-0420" + (10000000..99999999).random()
                                            else -> "DOC-" + (100000..999999).random()
                                        }
                                        val sampleIssuer = when {
                                            documentType.contains("Aadhaar", ignoreCase = true) -> "UIDAI, Govt of India"
                                            documentType.contains("PAN", ignoreCase = true) -> "Income Tax Dept, Govt of India"
                                            documentType.contains("Passport", ignoreCase = true) -> "Ministry of External Affairs, New Delhi"
                                            documentType.contains("Driving", ignoreCase = true) -> "Transport Department, Delhi NCT"
                                            else -> "Govt Authority / Registrar"
                                        }
                                        documentNumber = sampleNumber
                                        issuer = sampleIssuer
                                        if (title.isBlank() || title == "New Document") {
                                            title = documentType
                                        }
                                        ocrExtracted = true
                                        isScanningOcr = false
                                        ocrSuccessMessage = "Auto-extracted from document image!"
                                    }
                                },
                                enabled = !isScanningOcr,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (ocrExtracted) VaultCardBorder else ElectricCyan),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("btn_auto_scan_ocr")
                            ) {
                                if (isScanningOcr) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = if (ocrExtracted) TextSecondary else Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (ocrExtracted) "Re-Scan" else "Auto-Scan",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ocrExtracted) TextSecondary else Color.Black
                                    )
                                }
                            }
                        }

                        if (isScanningOcr) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = ElectricCyan, strokeWidth = 1.5.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Analyzing image structure & extracting document text...",
                                    fontSize = 11.sp,
                                    color = ElectricCyan
                                )
                            }
                        }
                    }
                }

                // Document Type Field (Free-text with quick suggestions)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = documentType,
                        onValueChange = {
                            documentType = it
                            if (title.isBlank() || title == "New Document") {
                                title = it
                            }
                        },
                        label = { Text("Document Type (Free Text) *") },
                        placeholder = { Text("e.g. Aadhaar, PAN Card, Passport, Degree") },
                        singleLine = true,
                        colors = custodiaTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_document_type")
                    )

                    // Quick suggestion pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        POPULAR_DOC_TYPES.forEach { presetType ->
                            val isSelected = documentType.equals(presetType, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) TrustTeal.copy(alpha = 0.25f) else VaultSurface)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) TrustTeal else VaultCardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        documentType = presetType
                                        title = presetType
                                        issuer = when {
                                            presetType.contains("Aadhaar") -> "UIDAI, Govt of India"
                                            presetType.contains("PAN") -> "Income Tax Dept"
                                            presetType.contains("Passport") -> "Ministry of External Affairs"
                                            presetType.contains("Driving") -> "Transport Authority"
                                            else -> issuer
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = presetType,
                                    fontSize = 10.5.sp,
                                    color = if (isSelected) TrustTeal else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Document Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title *") },
                    placeholder = { Text("e.g. Official Aadhaar Card") },
                    singleLine = true,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_document_title")
                )

                // Document Number & Issuing Authority
                OutlinedTextField(
                    value = documentNumber,
                    onValueChange = { documentNumber = it },
                    label = { Text("Document Number *") },
                    placeholder = { Text("e.g. 4829 7710 3921") },
                    singleLine = true,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_document_number")
                )

                OutlinedTextField(
                    value = issuer,
                    onValueChange = { issuer = it },
                    label = { Text("Issuing Authority") },
                    placeholder = { Text("e.g. UIDAI / Income Tax Dept / CBSE") },
                    singleLine = true,
                    colors = custodiaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Issue Date & Expiry Date Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = issueDate,
                        onValueChange = { issueDate = it },
                        label = { Text("Issue Date") },
                        placeholder = { Text("DD Mon YYYY") },
                        singleLine = true,
                        colors = custodiaTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )

                    if (!isPermanent) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            label = { Text("Expiry Date") },
                            placeholder = { Text("DD Mon YYYY") },
                            singleLine = true,
                            colors = custodiaTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Permanent / No Expiry Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPermanent = !isPermanent }
                ) {
                    Checkbox(
                        checked = isPermanent,
                        onCheckedChange = { isPermanent = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = TrustTeal,
                            uncheckedColor = TextSecondary
                        )
                    )
                    Text(
                        text = "Permanent Document (No Expiry Date)",
                        fontSize = 12.5.sp,
                        color = TextPrimary
                    )
                }

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Remarks (Optional)") },
                    placeholder = { Text("e.g. Stored in blue folder, original with notary") },
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
                        colors = ButtonDefaults.buttonColors(containerColor = VaultSurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() && documentType.isNotBlank() && documentNumber.isNotBlank()) {
                                onSave(
                                    documentToEdit?.id,
                                    selectedMember.id,
                                    title.trim(),
                                    documentType.trim(),
                                    documentNumber.trim(),
                                    issuer.trim().ifBlank { "Official Issuer" },
                                    issueDate.trim().ifBlank { "01 Jan 2020" },
                                    if (isPermanent) null else expiryDate.trim(),
                                    notes.trim(),
                                    ocrExtracted
                                )
                            }
                        },
                        enabled = title.isNotBlank() && documentType.isNotBlank() && documentNumber.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_save_document")
                    ) {
                        Text(
                            text = if (documentToEdit == null) "Save Document" else "Update Document",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
