package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DocumentItem
import com.example.data.FileStorageHelper
import com.example.data.FamilyMemberProfile
import com.example.data.MedicalEntry
import com.example.data.MemberSignature
import com.example.data.PdfExportHelper
import com.example.ui.MemberTab
import com.example.ui.components.EditBaselineMedicalModal
import com.example.ui.components.SignatureDisplayCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TrustTeal
import com.example.ui.theme.VerifiedGreen

@Composable
fun MemberDetailScreen(
    member: FamilyMemberProfile,
    activeTab: MemberTab,
    documents: List<DocumentItem>,
    signature: MemberSignature?,
    medicalEntries: List<MedicalEntry>,
    onTabSelected: (MemberTab) -> Unit,
    onBackClick: () -> Unit,
    onEditMemberClick: (FamilyMemberProfile) -> Unit,
    onExportMemberPdf: (Context, FamilyMemberProfile) -> Unit,
    // Documents
    onAddDocumentClick: () -> Unit,
    onViewDocumentClick: (DocumentItem) -> Unit,
    onEditDocumentClick: (DocumentItem) -> Unit,
    onDeleteDocumentClick: (String) -> Unit,
    onExportDocumentPdf: (Context, DocumentItem) -> Unit,
    // Signature
    onDrawSignatureClick: () -> Unit,
    onDeleteSignatureClick: () -> Unit,
    onExportSignaturePdf: (Context, MemberSignature) -> Unit,
    // Medical
    onAddMedicalEntryClick: () -> Unit,
    onEditMedicalEntryClick: (MedicalEntry) -> Unit,
    onDeleteMedicalEntryClick: (String) -> Unit,
    onExportMedicalEntryPdf: (Context, MedicalEntry) -> Unit,
    onUpdateBaselineMedical: (
        memberId: String,
        bloodGroup: String,
        allergies: String,
        chronicConditions: String,
        currentMedications: String,
        pastIllnesses: String,
        doctorNotes: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showBaselineEditModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
    ) {
        // Top Back Navigation Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("btn_back_to_family")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to Family", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Back to Family Members",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Main Content Scroll
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Member Header Card
            item {
                MemberProfileHeaderCard(
                    member = member,
                    onEditClick = { onEditMemberClick(member) },
                    onExportPdfClick = { onExportMemberPdf(context, member) }
                )
            }

            // 3-Tab Selector
            item {
                TabRow(
                    selectedTabIndex = activeTab.ordinal,
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                    contentColor = TrustTeal,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                            color = TrustTeal,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                ) {
                    MemberTab.values().forEach { tab ->
                        val isSelected = activeTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = { onTabSelected(tab) },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val countSuffix = when (tab) {
                                        MemberTab.DOCUMENTS -> " (${documents.size})"
                                        MemberTab.SIGNATURE -> if (signature != null) " (✓)" else ""
                                        MemberTab.MEDICAL -> " (${medicalEntries.size})"
                                    }
                                    Text(
                                        text = "${tab.title}$countSuffix",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp,
                                        color = if (isSelected) TrustTeal else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // TAB 1: DOCUMENTS
            if (activeTab == MemberTab.DOCUMENTS) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DOCUMENTS (${documents.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )

                        Button(
                            onClick = onAddDocumentClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("btn_add_document")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Document", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                if (documents.isEmpty()) {
                    item {
                        EmptyTabBox(
                            icon = Icons.Default.Description,
                            title = "No Documents Added",
                            description = "Add identity cards, certificates, or policies for ${member.name}. Use OCR auto-scan for instant pre-fill.",
                            actionLabel = "Add Document (OCR)",
                            onAction = onAddDocumentClick
                        )
                    }
                } else {
                    items(documents, key = { it.id }) { doc ->
                        DocumentItemCard(
                            document = doc,
                            onViewClick = { onViewDocumentClick(doc) },
                            onEditClick = { onEditDocumentClick(doc) },
                            onDeleteClick = { onDeleteDocumentClick(doc.id) },
                            onExportPdfClick = { onExportDocumentPdf(context, doc) }
                        )
                    }
                }
            }

            // TAB 2: SIGNATURE
            if (activeTab == MemberTab.SIGNATURE) {
                item {
                    SignatureDisplayCard(
                        signature = signature,
                        member = member,
                        onDrawClick = onDrawSignatureClick,
                        onDeleteClick = onDeleteSignatureClick,
                        onShareClick = {
                            signature?.let { FileStorageHelper.shareSignature(context, it, member) }
                        },
                        onDownloadClick = {
                            signature?.let { FileStorageHelper.downloadSignatureToDevice(context, it, member) }
                        }
                    )
                }
            }

            // TAB 3: MEDICAL HISTORY
            if (activeTab == MemberTab.MEDICAL) {
                // Baseline Medical Profile Card
                item {
                    BaselineHealthCard(
                        member = member,
                        onEditClick = { showBaselineEditModal = true }
                    )
                }

                // Consultation & Records Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONSULTATION & MEDICAL RECORDS (${medicalEntries.size})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )

                        Button(
                            onClick = onAddMedicalEntryClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("btn_add_medical_record")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Record", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                if (medicalEntries.isEmpty()) {
                    item {
                        EmptyTabBox(
                            icon = Icons.Default.MedicalServices,
                            title = "No Consultation Records",
                            description = "Record doctor visits, prescriptions, surgeries, and attached lab test reports.",
                            actionLabel = "Add First Record",
                            onAction = onAddMedicalEntryClick
                        )
                    }
                } else {
                    items(medicalEntries, key = { it.id }) { entry ->
                        MedicalEntryCard(
                            entry = entry,
                            member = member,
                            onEditClick = { onEditMedicalEntryClick(entry) },
                            onDeleteClick = { onDeleteMedicalEntryClick(entry.id) },
                            onExportPdfClick = { onExportMedicalEntryPdf(context, entry) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Baseline Medical Edit Dialog
    if (showBaselineEditModal) {
        EditBaselineMedicalModal(
            member = member,
            onDismiss = { showBaselineEditModal = false },
            onSave = { id, bg, allergies, chronic, meds, past, notes ->
                onUpdateBaselineMedical(id, bg, allergies, chronic, meds, past, notes)
                showBaselineEditModal = false
            }
        )
    }
}

@Composable
private fun MemberProfileHeaderCard(
    member: FamilyMemberProfile,
    onEditClick: () -> Unit,
    onExportPdfClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(member.avatarColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.avatarInitials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = member.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TrustTeal.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(member.relationshipLabel, color = TrustTeal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Blood: ${member.bloodGroup}", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 11.5.sp, color = TrustTeal)
                    }

                    Button(
                        onClick = onExportPdfClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Divider(color = androidx.compose.material3.MaterialTheme.colorScheme.outline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Date of Birth", fontSize = 10.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f))
                    Text(member.dob, fontSize = 12.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                }

                if (member.phone.isNotBlank()) {
                    Column {
                        Text("Phone", fontSize = 10.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f))
                        Text(member.phone, fontSize = 12.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                    }
                }

                if (member.email.isNotBlank()) {
                    Column {
                        Text("Email", fontSize = 10.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f))
                        Text(member.email, fontSize = 12.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun BaselineHealthCard(
    member: FamilyMemberProfile,
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Baseline Health Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Profile", fontSize = 11.sp, color = TrustTeal)
                }
            }

            Divider(color = androidx.compose.material3.MaterialTheme.colorScheme.outline)

            // Health Details Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Allergies", fontSize = 10.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f))
                    Text(member.allergies, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Chronic Conditions", fontSize = 10.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f))
                    Text(member.chronicConditions, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Daily Medications", fontSize = 10.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f))
                    Text(member.currentMedications, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Past Illnesses / Surgeries", fontSize = 10.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f))
                    Text(member.pastIllnessesOrSurgeries, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                }
            }

            if (member.doctorNotes.isNotBlank()) {
                Column {
                    Text("Doctor's Key Advice", fontSize = 10.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f))
                    Text(member.doctorNotes, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DocumentItemCard(
    document: DocumentItem,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportPdfClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("card_doc_${document.title.replace(" ", "_")}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Title & Type badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TrustTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = document.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = document.issuer,
                            fontSize = 11.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TrustTeal.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = document.documentType,
                        color = TrustTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                }
            }

            // Document Number & OCR badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("No: ", fontSize = 11.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f))
                    Text(
                        text = document.documentNumber,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = ElectricCyan,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { clipboardManager.setText(AnnotatedString(document.documentNumber)) }
                    )
                }

                if (document.ocrExtracted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("OCR Verified", fontSize = 10.sp, color = VerifiedGreen, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Validity Dates & Action Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Valid: ${document.issueDate} - ${document.expiryDate ?: "Permanent"}",
                    fontSize = 11.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onViewClick, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = "View", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { FileStorageHelper.shareDocument(context, document) }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share original", tint = TrustTeal, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { FileStorageHelper.downloadDocumentToDevice(context, document) }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEditClick, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicalEntryCard(
    entry: MedicalEntry,
    member: FamilyMemberProfile,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportPdfClick: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("card_medical_${entry.title.replace(" ", "_")}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = entry.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${entry.doctorOrClinic} • ${entry.date}",
                            fontSize = 11.5.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    IconButton(onClick = {
                        val path = entry.attachedReportPath
                        if (path != null) FileStorageHelper.openFile(context, path)
                        else FileStorageHelper.openFile(context, PdfExportHelper.exportSingleMedicalEntryPdf(context, member, entry).absolutePath)
                    }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = "View medical record", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { FileStorageHelper.shareMedicalEntry(context, entry, member) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share medical record", tint = TrustTeal, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { FileStorageHelper.downloadMedicalEntryToDevice(context, entry, member) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Download, contentDescription = "Download medical record", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Clinical Notes
            Text(
                text = entry.notes,
                fontSize = 12.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            // Attached Report File Tag (if any)
            if (entry.attachedReportName != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = entry.attachedReportName,
                        fontSize = 11.sp,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTabBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f), modifier = Modifier.size(36.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
            Text(
                text = description,
                fontSize = 11.5.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TrustTeal)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(actionLabel, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
            }
        }
    }
}
