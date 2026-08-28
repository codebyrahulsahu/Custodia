package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.DocumentItem
import com.example.data.FamilyMemberProfile
import com.example.data.FileStorageHelper
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
import java.io.File

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
        ocrExtracted: Boolean,
        filePath: String?,
        fileName: String?,
        fileSize: String,
        fileType: String,
        isImage: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var documentType by remember { mutableStateOf(documentToEdit?.documentType ?: "Aadhaar Card") }
    var title by remember { mutableStateOf(documentToEdit?.title ?: "Aadhaar Card") }
    var documentNumber by remember { mutableStateOf(documentToEdit?.documentNumber ?: "") }
    var issuer by remember { mutableStateOf(documentToEdit?.issuer ?: "UIDAI, Govt of India") }
    var issueDate by remember { mutableStateOf(documentToEdit?.issueDate ?: "15 Jan 2020") }
    var isPermanent by remember { mutableStateOf(documentToEdit?.expiryDate == null) }
    var expiryDate by remember { mutableStateOf(documentToEdit?.expiryDate ?: "14 Jan 2030") }
    var notes by remember { mutableStateOf(documentToEdit?.notes ?: "") }
    var ocrExtracted by remember { mutableStateOf(documentToEdit?.ocrExtracted ?: false) }

    // File Management State
    var attachedFilePath by remember { mutableStateOf(documentToEdit?.filePath) }
    var attachedFileName by remember { mutableStateOf(documentToEdit?.fileName) }
    var attachedFileSize by remember { mutableStateOf(documentToEdit?.fileSize ?: "1.2 MB") }
    var attachedFileType by remember { mutableStateOf(documentToEdit?.fileType ?: "PDF / Image") }
    var isAttachedImage by remember { mutableStateOf(documentToEdit?.isImage ?: false) }

    var isScanningOcr by remember { mutableStateOf(false) }
    var ocrStatusMessage by remember { mutableStateOf<String?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // File Manager Launcher (Pick any Document/PDF/Image)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedInfo = FileStorageHelper.saveUriToVault(
                context = context,
                sourceUri = uri,
                targetFolder = FileStorageHelper.getDocumentsDir(context)
            )
            if (savedInfo != null) {
                attachedFilePath = savedInfo.filePath
                attachedFileName = savedInfo.fileName
                attachedFileSize = savedInfo.fileSizeFormatted
                attachedFileType = if (savedInfo.isImage) "Image" else "Document (${savedInfo.mimeType.substringAfterLast("/")})"
                isAttachedImage = savedInfo.isImage
                ocrStatusMessage = "File uploaded from File Manager. Tap 'Run OCR Scan' to auto-extract text."
            }
        }
    }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraFile != null && tempCameraFile!!.exists()) {
            val destFolder = FileStorageHelper.getDocumentsDir(context)
            val destFile = File(destFolder, "camera_${System.currentTimeMillis()}_${tempCameraFile!!.name}")
            tempCameraFile!!.copyTo(destFile, overwrite = true)

            attachedFilePath = destFile.absolutePath
            attachedFileName = destFile.name
            attachedFileSize = FileStorageHelper.formatFileSize(destFile.length())
            attachedFileType = "Photo / Camera Scan"
            isAttachedImage = true
            ocrStatusMessage = "Camera photo captured. Tap 'Run OCR Scan' to auto-extract text."
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

                // FILE UPLOAD SECTION (File Manager & Camera)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VaultSurface)
                        .border(1.dp, VaultCardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ATTACH DOCUMENT FILE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )

                            if (attachedFilePath != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VerifiedGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("File Attached", fontSize = 10.sp, color = VerifiedGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Upload buttons: File Manager + Camera
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // File Manager Upload
                            Button(
                                onClick = {
                                    filePickerLauncher.launch("*/*")
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VaultNavyDark),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("btn_upload_from_file_manager")
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("File Manager", fontSize = 11.5.sp, color = ElectricCyan, fontWeight = FontWeight.SemiBold)
                            }

                            // Camera Photo Capture
                            Button(
                                onClick = {
                                    try {
                                        val (uri, file) = FileStorageHelper.createTempCameraUri(context)
                                        tempCameraUri = uri
                                        tempCameraFile = file
                                        cameraLauncher.launch(uri)
                                    } catch (e: Exception) {
                                        ocrStatusMessage = "Camera error: ${e.message}"
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VaultNavyDark),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("btn_capture_with_camera")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Take Photo", fontSize = 11.5.sp, color = TrustTeal, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Attached File Card / Preview
                        if (attachedFilePath != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VaultNavyDark)
                                    .border(1.dp, VaultCardBorder, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isAttachedImage && attachedFilePath != null) {
                                        AsyncImage(
                                            model = File(attachedFilePath!!),
                                            contentDescription = "Document Thumbnail",
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .border(1.dp, VaultCardBorder, RoundedCornerShape(6.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(TrustTeal.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Description, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(22.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = attachedFileName ?: "Document File",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "$attachedFileSize • $attachedFileType",
                                            fontSize = 10.5.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            FileStorageHelper.openFile(context, attachedFilePath!!)
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = "Open File", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            attachedFilePath = null
                                            attachedFileName = null
                                            isAttachedImage = false
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove File", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // SMART OCR SCAN CARD (Explicit User Triggered)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(VaultSurface)
                        .border(
                            1.dp,
                            if (ocrExtracted) VerifiedGreen.copy(alpha = 0.4f) else ElectricCyan.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (ocrExtracted) Icons.Default.CheckCircle else Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = if (ocrExtracted) VerifiedGreen else ElectricCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (ocrExtracted) "OCR Extraction Complete" else "Smart OCR Text Extraction",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = if (ocrExtracted) VerifiedGreen else TextPrimary
                                    )
                                    Text(
                                        text = if (attachedFilePath != null) "Ready to scan attached file" else "Attach a file above to run OCR",
                                        fontSize = 10.5.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (attachedFilePath == null && attachedFileName == null) {
                                        ocrStatusMessage = "Please upload a document from File Manager or take a Photo first!"
                                        return@Button
                                    }

                                    coroutineScope.launch {
                                        isScanningOcr = true
                                        ocrStatusMessage = "Scanning and analyzing document text..."
                                        delay(1200)

                                        val ocrResult = FileStorageHelper.performSmartOcr(
                                            context = context,
                                            filePath = attachedFilePath,
                                            fileName = attachedFileName
                                        )

                                        if (ocrResult.detectedType != null) {
                                            documentType = ocrResult.detectedType
                                        }
                                        if (ocrResult.detectedNumber != null) {
                                            documentNumber = ocrResult.detectedNumber
                                        }
                                        if (ocrResult.detectedTitle != null) {
                                            title = ocrResult.detectedTitle
                                        }
                                        if (ocrResult.detectedIssuer != null) {
                                            issuer = ocrResult.detectedIssuer
                                        }
                                        if (ocrResult.detectedIssueDate != null) {
                                            issueDate = ocrResult.detectedIssueDate
                                        }
                                        if (ocrResult.detectedExpiryDate != null) {
                                            expiryDate = ocrResult.detectedExpiryDate
                                            isPermanent = false
                                        }

                                        ocrExtracted = true
                                        isScanningOcr = false
                                        ocrStatusMessage = "OCR Successful! Fields filled automatically from your document."
                                    }
                                },
                                enabled = !isScanningOcr,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (ocrExtracted) VaultCardBorder else ElectricCyan
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("btn_auto_scan_ocr")
                            ) {
                                if (isScanningOcr) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        Icons.Default.AutoFixHigh,
                                        contentDescription = null,
                                        tint = if (ocrExtracted) TextSecondary else Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (ocrExtracted) "Re-Scan" else "Run OCR",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ocrExtracted) TextSecondary else Color.Black
                                    )
                                }
                            }
                        }

                        if (ocrStatusMessage != null) {
                            Text(
                                text = ocrStatusMessage!!,
                                fontSize = 11.sp,
                                color = if (ocrExtracted) VerifiedGreen else ElectricCyan,
                                lineHeight = 14.sp
                            )
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
                    placeholder = { Text("e.g. Original physical copy in safe locker") },
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
                                    ocrExtracted,
                                    attachedFilePath,
                                    attachedFileName,
                                    attachedFileSize,
                                    attachedFileType,
                                    isAttachedImage
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
