package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.FamilyMemberProfile
import com.example.data.FileStorageHelper
import com.example.data.MemberSignature
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrustTeal
import com.example.ui.theme.VaultCardBorder
import com.example.ui.theme.VaultNavyDark
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VerifiedGreen
import java.io.File

@Composable
fun SignatureDisplayCard(
    signature: MemberSignature?,
    member: FamilyMemberProfile,
    onDrawClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDownloadPdfClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VaultSurface)
            .border(1.dp, VaultCardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        if (signature != null && (signature.pathPoints.isNotEmpty() || !signature.imageUri.isNullOrBlank())) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Verified Signature Specimen",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = VerifiedGreen,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = "Recorded on ${signature.createdDate} • Type: ${signature.signatureType}",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Signature", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Signature Canvas / Image Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!signature.imageUri.isNullOrBlank()) {
                        val file = File(signature.imageUri)
                        if (file.exists()) {
                            AsyncImage(
                                model = file,
                                contentDescription = "Signature Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            AsyncImage(
                                model = signature.imageUri,
                                contentDescription = "Signature Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else if (signature.pathPoints.isNotEmpty()) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            for (stroke in signature.pathPoints) {
                                if (stroke.isNotEmpty()) {
                                    val path = Path()
                                    path.moveTo(stroke.first().x, stroke.first().y)
                                    for (i in 1 until stroke.size) {
                                        path.lineTo(stroke[i].x, stroke[i].y)
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color(0xFF1E3A8A), // Blue ink
                                        style = Stroke(
                                            width = 3.5f,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "✍️ ${signature.signerName}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }

                // Metadata & Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Certificate: ${signature.certificateTag}",
                        fontSize = 10.5.sp,
                        color = TextMuted
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onDrawClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VaultNavyDark),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Draw, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Update / Re-Upload", fontSize = 11.sp, color = TrustTeal)
                        }

                        Button(
                            onClick = onDownloadPdfClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Download PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        } else {
            // Empty signature state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TrustTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = null,
                        tint = TrustTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "No Signature Added Yet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Draw or upload an image of ${member.name}'s signature from File Manager or Camera for verification and self-attestation.",
                    fontSize = 11.5.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDrawClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                    modifier = Modifier.testTag("btn_add_signature")
                ) {
                    Icon(Icons.Default.Draw, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Draw or Upload Signature", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.5.sp)
                }
            }
        }
    }
}

enum class SignatureInputMode(val title: String) {
    DRAW("Draw on Screen"),
    UPLOAD_IMAGE("File Manager / Camera")
}

@Composable
fun SignaturePadDialog(
    member: FamilyMemberProfile,
    onDismiss: () -> Unit,
    onSaveDrawn: (strokes: List<List<Offset>>) -> Unit,
    onSaveImageUri: (imageUri: String) -> Unit
) {
    val context = LocalContext.current
    var inputMode by remember { mutableStateOf(SignatureInputMode.DRAW) }

    // Drawing state
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedInkColor by remember { mutableStateOf(Color(0xFF1E3A8A)) } // Deep Blue

    // Upload state
    var uploadedImagePath by remember { mutableStateOf<String?>(null) }
    var uploadedImageName by remember { mutableStateOf<String?>(null) }
    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }

    // File Manager launcher for signature image
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedInfo = FileStorageHelper.saveUriToVault(
                context = context,
                sourceUri = uri,
                targetFolder = FileStorageHelper.getSignaturesDir(context)
            )
            if (savedInfo != null) {
                uploadedImagePath = savedInfo.filePath
                uploadedImageName = savedInfo.fileName
                uploadStatusMessage = "Signature image uploaded from File Manager."
            }
        }
    }

    // Camera launcher for capturing physical signature paper
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraFile != null && tempCameraFile!!.exists()) {
            val destFolder = FileStorageHelper.getSignaturesDir(context)
            val destFile = File(destFolder, "sig_camera_${System.currentTimeMillis()}.jpg")
            tempCameraFile!!.copyTo(destFile, overwrite = true)

            uploadedImagePath = destFile.absolutePath
            uploadedImageName = destFile.name
            uploadStatusMessage = "Photo of signature captured successfully."
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
                            Icon(Icons.Default.Draw, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Signature Vault",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Member: ${member.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                // Mode Tabs: Draw vs Upload Image
                TabRow(
                    selectedTabIndex = inputMode.ordinal,
                    containerColor = VaultSurface,
                    contentColor = TrustTeal,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[inputMode.ordinal]),
                            color = TrustTeal,
                            height = 2.5.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, VaultCardBorder, RoundedCornerShape(8.dp))
                ) {
                    SignatureInputMode.values().forEach { mode ->
                        val isSelected = inputMode == mode
                        Tab(
                            selected = isSelected,
                            onClick = { inputMode = mode },
                            text = {
                                Text(
                                    text = mode.title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TrustTeal else TextSecondary
                                )
                            }
                        )
                    }
                }

                if (inputMode == SignatureInputMode.DRAW) {
                    // Ink Selection & Clear Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ink:", fontSize = 12.sp, color = TextSecondary)
                            listOf(
                                Color(0xFF1E3A8A) to "Blue",
                                Color(0xFF0F172A) to "Black",
                                Color(0xFF6B21A8) to "Purple"
                            ).forEach { (color, _) ->
                                val isSelected = selectedInkColor == color
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) TrustTeal else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedInkColor = color }
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                strokes.clear()
                                currentStroke = emptyList()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Clear Canvas", tint = TextSecondary)
                        }
                    }

                    // Drawing Canvas Surface
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentStroke = listOf(offset)
                                    },
                                    onDrag = { change, _ ->
                                        currentStroke = currentStroke + change.position
                                    },
                                    onDragEnd = {
                                        if (currentStroke.isNotEmpty()) {
                                            strokes.add(currentStroke)
                                            currentStroke = emptyList()
                                        }
                                    }
                                )
                            }
                            .testTag("canvas_signature_pad")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw completed strokes
                            for (stroke in strokes) {
                                if (stroke.isNotEmpty()) {
                                    val path = Path()
                                    path.moveTo(stroke.first().x, stroke.first().y)
                                    for (i in 1 until stroke.size) {
                                        path.lineTo(stroke[i].x, stroke[i].y)
                                    }
                                    drawPath(
                                        path = path,
                                        color = selectedInkColor,
                                        style = Stroke(
                                            width = 3.5f,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }

                            // Draw current active stroke
                            if (currentStroke.isNotEmpty()) {
                                val path = Path()
                                path.moveTo(currentStroke.first().x, currentStroke.first().y)
                                for (i in 1 until currentStroke.size) {
                                    path.lineTo(currentStroke[i].x, currentStroke[i].y)
                                }
                                drawPath(
                                    path = path,
                                    color = selectedInkColor,
                                    style = Stroke(
                                        width = 3.5f,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        if (strokes.isEmpty() && currentStroke.isEmpty()) {
                            Text(
                                text = "Sign here with finger / stylus",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    // Quick specimen option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VaultSurface)
                            .clickable {
                                val sampleStrokes = listOf(
                                    listOf(Offset(30f, 70f), Offset(60f, 40f), Offset(90f, 80f), Offset(120f, 40f), Offset(160f, 80f)),
                                    listOf(Offset(160f, 80f), Offset(190f, 45f), Offset(220f, 70f), Offset(260f, 35f)),
                                    listOf(Offset(40f, 90f), Offset(270f, 85f))
                                )
                                strokes.clear()
                                strokes.addAll(sampleStrokes)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Or load signature specimen for ${member.name}",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Load Specimen",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrustTeal
                        )
                    }
                } else {
                    // UPLOAD SIGNATURE IMAGE MODE
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Upload a photo or scanned copy of ${member.name}'s signature from your file manager or take a photo of a signed paper.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    filePickerLauncher.launch("image/*")
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VaultSurface),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("btn_upload_sig_file_manager")
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("File Manager", fontSize = 11.5.sp, color = ElectricCyan, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val (uri, file) = FileStorageHelper.createTempCameraUri(context)
                                        tempCameraFile = file
                                        cameraLauncher.launch(uri)
                                    } catch (e: Exception) {
                                        uploadStatusMessage = "Camera error: ${e.message}"
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VaultSurface),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("btn_capture_sig_camera")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Take Photo", fontSize = 11.5.sp, color = TrustTeal, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Preview of uploaded signature image
                        if (uploadedImagePath != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, VaultCardBorder, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = File(uploadedImagePath!!),
                                    contentDescription = "Signature Preview",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            if (uploadStatusMessage != null) {
                                Text(
                                    text = uploadStatusMessage!!,
                                    fontSize = 11.5.sp,
                                    color = VerifiedGreen
                                )
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
                        colors = ButtonDefaults.buttonColors(containerColor = VaultSurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (inputMode == SignatureInputMode.DRAW) {
                                if (strokes.isNotEmpty()) {
                                    onSaveDrawn(strokes.toList())
                                }
                            } else {
                                if (uploadedImagePath != null) {
                                    onSaveImageUri(uploadedImagePath!!)
                                    onDismiss()
                                }
                            }
                        },
                        enabled = if (inputMode == SignatureInputMode.DRAW) strokes.isNotEmpty() else uploadedImagePath != null,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_save_signature_confirm")
                    ) {
                        Text("Save Signature", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
