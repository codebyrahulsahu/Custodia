package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CustodiaViewModel
import com.example.ui.components.AddEditDocumentDialog
import com.example.ui.components.AddEditMedicalEntryDialog
import com.example.ui.components.AddEditMemberModal
import com.example.ui.components.AppHeader
import com.example.ui.components.FamilyTreeModal
import com.example.ui.components.GoogleDriveBackupModal
import com.example.ui.components.SignaturePadDialog
import com.example.ui.components.ViewDocumentModal
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemberDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TrustTeal
import com.example.ui.theme.VaultNavy

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CustodiaApp()
            }
        }
    }
}

@Composable
fun CustodiaApp(viewModel: CustodiaViewModel = viewModel()) {
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val signatures by viewModel.signatures.collectAsStateWithLifecycle()
    val medicalEntries by viewModel.medicalEntries.collectAsStateWithLifecycle()
    val driveAccount by viewModel.driveAccount.collectAsStateWithLifecycle()
    val driveBackups by viewModel.driveBackups.collectAsStateWithLifecycle()
    val isBackingUp by viewModel.isBackingUp.collectAsStateWithLifecycle()

    val selectedMemberId by viewModel.selectedMemberId.collectAsStateWithLifecycle()
    val selectedMember by viewModel.selectedMember.collectAsStateWithLifecycle()
    val activeMemberTab by viewModel.activeMemberTab.collectAsStateWithLifecycle()

    val selectedMemberDocuments by viewModel.selectedMemberDocuments.collectAsStateWithLifecycle()
    val selectedMemberSignature by viewModel.selectedMemberSignature.collectAsStateWithLifecycle()
    val selectedMemberMedicalEntries by viewModel.selectedMemberMedicalEntries.collectAsStateWithLifecycle()

    // Modals
    val showAddMemberModal by viewModel.showAddMemberModal.collectAsStateWithLifecycle()
    val editingMember by viewModel.editingMember.collectAsStateWithLifecycle()
    val showAddDocModal by viewModel.showAddDocModal.collectAsStateWithLifecycle()
    val editingDoc by viewModel.editingDoc.collectAsStateWithLifecycle()
    val viewingDoc by viewModel.viewingDoc.collectAsStateWithLifecycle()
    val showSignatureDialog by viewModel.showSignatureDialog.collectAsStateWithLifecycle()
    val showAddMedicalModal by viewModel.showAddMedicalModal.collectAsStateWithLifecycle()
    val editingMedicalEntry by viewModel.editingMedicalEntry.collectAsStateWithLifecycle()
    val showFamilyTreeModal by viewModel.showFamilyTreeModal.collectAsStateWithLifecycle()
    val showDriveBackupModal by viewModel.showDriveBackupModal.collectAsStateWithLifecycle()

    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppHeader(
                driveAccount = driveAccount,
                onFamilyTreeClick = { viewModel.setShowFamilyTreeModal(true) },
                onDriveBackupClick = { viewModel.setShowDriveBackupModal(true) },
                onAddMemberClick = { viewModel.openAddMemberModal() },
                showAddMemberButton = selectedMemberId == null
            )
        },
        containerColor = com.example.ui.theme.BackgroundWhite
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedMember,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { member ->
                if (member == null) {
                    HomeScreen(
                        familyMembers = familyMembers,
                        documents = documents,
                        signatures = signatures,
                        medicalEntries = medicalEntries,
                        onSelectMember = { memberId -> viewModel.selectMember(memberId) },
                        onAddMemberClick = { viewModel.openAddMemberModal() },
                        onEditMemberClick = { mem -> viewModel.openEditMemberModal(mem) },
                        onDeleteMemberClick = { id -> viewModel.deleteMember(id) },
                        onExportMemberPdf = { ctx, mem -> viewModel.exportMemberCompletePdf(ctx, mem) }
                    )
                } else {
                    MemberDetailScreen(
                        member = member,
                        activeTab = activeMemberTab,
                        documents = selectedMemberDocuments,
                        signature = selectedMemberSignature,
                        medicalEntries = selectedMemberMedicalEntries,
                        onTabSelected = { tab -> viewModel.setActiveMemberTab(tab) },
                        onBackClick = { viewModel.selectMember(null) },
                        onEditMemberClick = { mem -> viewModel.openEditMemberModal(mem) },
                        onExportMemberPdf = { ctx, mem -> viewModel.exportMemberCompletePdf(ctx, mem) },
                        // Documents
                        onAddDocumentClick = { viewModel.openAddDocumentModal() },
                        onViewDocumentClick = { doc -> viewModel.setViewingDocument(doc) },
                        onEditDocumentClick = { doc -> viewModel.openEditDocumentModal(doc) },
                        onDeleteDocumentClick = { docId -> viewModel.deleteDocument(docId) },
                        onExportDocumentPdf = { ctx, doc -> viewModel.exportDocumentPdf(ctx, doc) },
                        // Signature
                        onDrawSignatureClick = { viewModel.openSignatureDialog() },
                        onDeleteSignatureClick = { viewModel.deleteSignature(member.id) },
                        onExportSignaturePdf = { ctx, sig -> viewModel.exportSignaturePdf(ctx, sig) },
                        // Medical
                        onAddMedicalEntryClick = { viewModel.openAddMedicalModal() },
                        onEditMedicalEntryClick = { entry -> viewModel.openEditMedicalModal(entry) },
                        onDeleteMedicalEntryClick = { entryId -> viewModel.deleteMedicalEntry(entryId) },
                        onExportMedicalEntryPdf = { ctx, entry -> viewModel.exportMedicalEntryPdf(ctx, entry) },
                        onUpdateBaselineMedical = { id, bg, allergies, chronic, meds, past, notes ->
                            viewModel.updateMemberBaselineMedical(id, bg, allergies, chronic, meds, past, notes)
                        }
                    )
                }
            }

            // Notification / Feedback Banner
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                toastMessage?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp)),
                        color = TrustTeal,
                        shadowElevation = 8.dp
                    ) {
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Modals & Dialogs
    // -------------------------------------------------------------------------

    val context = androidx.compose.ui.platform.LocalContext.current

    if (showAddMemberModal) {
        AddEditMemberModal(
            memberToEdit = editingMember,
            onDismiss = { viewModel.closeMemberModal() },
            onSave = { id, name, relationship, dob, bloodGroup, phone, email, colorHex ->
                viewModel.saveMember(id, name, relationship, dob, bloodGroup, phone, email, colorHex)
            }
        )
    }

    if (showAddDocModal && selectedMember != null) {
        AddEditDocumentDialog(
            selectedMember = selectedMember!!,
            documentToEdit = editingDoc,
            onDismiss = { viewModel.closeDocumentModal() },
            onSave = { id, memberId, title, docType, docNum, issuer, issueDate, expiryDate, notes, ocrExtracted, filePath, fileName, fileSize, fileType, isImage ->
                viewModel.saveDocument(
                    id = id,
                    memberId = memberId,
                    title = title,
                    documentType = docType,
                    documentNumber = docNum,
                    issuer = issuer,
                    issueDate = issueDate,
                    expiryDate = expiryDate,
                    notes = notes,
                    ocrExtracted = ocrExtracted,
                    filePath = filePath,
                    fileName = fileName,
                    fileSize = fileSize,
                    fileType = fileType,
                    isImage = isImage
                )
            }
        )
    }

    if (viewingDoc != null) {
        ViewDocumentModal(
            document = viewingDoc!!,
            onDismiss = { viewModel.setViewingDocument(null) },
            onEditClick = { doc ->
                viewModel.setViewingDocument(null)
                viewModel.openEditDocumentModal(doc)
            },
            onDeleteClick = { doc ->
                viewModel.setViewingDocument(null)
                viewModel.deleteDocument(doc.id)
            },
            onDownloadPdfClick = { doc ->
                viewModel.exportDocumentPdf(context, doc)
            }
        )
    }

    if (showSignatureDialog && selectedMember != null) {
        SignaturePadDialog(
            member = selectedMember!!,
            onDismiss = { viewModel.closeSignatureDialog() },
            onSaveDrawn = { strokes ->
                viewModel.saveDrawnSignature(selectedMember!!.id, strokes)
            },
            onSaveImageUri = { uri ->
                viewModel.saveUploadedSignatureImage(selectedMember!!.id, uri)
            }
        )
    }

    if (showAddMedicalModal && selectedMember != null) {
        AddEditMedicalEntryDialog(
            selectedMember = selectedMember!!,
            entryToEdit = editingMedicalEntry,
            onDismiss = { viewModel.closeMedicalModal() },
            onSave = { id, memberId, date, title, doctor, notes, attachedReportName, attachedReportPath ->
                viewModel.saveMedicalEntry(
                    id = id,
                    memberId = memberId,
                    date = date,
                    title = title,
                    doctorOrClinic = doctor,
                    notes = notes,
                    attachedReportName = attachedReportName,
                    attachedReportPath = attachedReportPath
                )
            }
        )
    }

    if (showFamilyTreeModal) {
        FamilyTreeModal(
            familyMembers = familyMembers,
            onDismiss = { viewModel.setShowFamilyTreeModal(false) },
            onSelectMember = { mem ->
                viewModel.selectMember(mem.id)
            }
        )
    }

    if (showDriveBackupModal) {
        GoogleDriveBackupModal(
            driveAccount = driveAccount,
            backups = driveBackups,
            isBackingUp = isBackingUp,
            onDismiss = { viewModel.setShowDriveBackupModal(false) },
            onBackupNow = { viewModel.performGoogleDriveBackup() },
            onRestoreBackup = { backup -> viewModel.restoreFromDriveBackup(backup) },
            onDeleteBackup = { id -> viewModel.deleteDriveBackup(id) }
        )
    }
}
