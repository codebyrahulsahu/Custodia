package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CustodiaRepository
import com.example.data.DocumentItem
import com.example.data.DriveAccountInfo
import com.example.data.DriveBackupInfo
import com.example.data.FamilyMemberProfile
import com.example.data.MedicalEntry
import com.example.data.MemberSignature
import com.example.data.PdfExportHelper
import com.example.data.RelationshipType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class MemberTab(val title: String) {
    DOCUMENTS("Documents"),
    SIGNATURE("Signature"),
    MEDICAL("Medical History")
}

class CustodiaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CustodiaRepository(application.applicationContext)

    // Core Data Flows from Room Repository
    val familyMembers: StateFlow<List<FamilyMemberProfile>> = repository.familyMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<DocumentItem>> = repository.documents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val signatures: StateFlow<List<MemberSignature>> = repository.signatures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicalEntries: StateFlow<List<MedicalEntry>> = repository.medicalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val driveAccount: StateFlow<DriveAccountInfo> = repository.driveAccount

    val driveBackups: StateFlow<List<DriveBackupInfo>> = repository.driveBackups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation & Selection State
    private val _selectedMemberId = MutableStateFlow<String?>(null)
    val selectedMemberId: StateFlow<String?> = _selectedMemberId.asStateFlow()

    private val _activeMemberTab = MutableStateFlow(MemberTab.DOCUMENTS)
    val activeMemberTab: StateFlow<MemberTab> = _activeMemberTab.asStateFlow()

    // Modals & Dialogs State
    private val _showAddMemberModal = MutableStateFlow(false)
    val showAddMemberModal: StateFlow<Boolean> = _showAddMemberModal.asStateFlow()

    private val _editingMember = MutableStateFlow<FamilyMemberProfile?>(null)
    val editingMember: StateFlow<FamilyMemberProfile?> = _editingMember.asStateFlow()

    private val _showAddDocModal = MutableStateFlow(false)
    val showAddDocModal: StateFlow<Boolean> = _showAddDocModal.asStateFlow()

    private val _editingDoc = MutableStateFlow<DocumentItem?>(null)
    val editingDoc: StateFlow<DocumentItem?> = _editingDoc.asStateFlow()

    private val _viewingDoc = MutableStateFlow<DocumentItem?>(null)
    val viewingDoc: StateFlow<DocumentItem?> = _viewingDoc.asStateFlow()

    private val _showSignatureDialog = MutableStateFlow(false)
    val showSignatureDialog: StateFlow<Boolean> = _showSignatureDialog.asStateFlow()

    private val _showAddMedicalModal = MutableStateFlow(false)
    val showAddMedicalModal: StateFlow<Boolean> = _showAddMedicalModal.asStateFlow()

    private val _editingMedicalEntry = MutableStateFlow<MedicalEntry?>(null)
    val editingMedicalEntry: StateFlow<MedicalEntry?> = _editingMedicalEntry.asStateFlow()

    private val _showFamilyTreeModal = MutableStateFlow(false)
    val showFamilyTreeModal: StateFlow<Boolean> = _showFamilyTreeModal.asStateFlow()

    private val _showDriveBackupModal = MutableStateFlow(false)
    val showDriveBackupModal: StateFlow<Boolean> = _showDriveBackupModal.asStateFlow()

    // Status Toast / SnackBar state
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    // Currently Selected Member details helper
    val selectedMember: StateFlow<FamilyMemberProfile?> = combine(
        familyMembers,
        _selectedMemberId
    ) { members, id ->
        if (id == null) null else members.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Filtered documents for selected member
    val selectedMemberDocuments: StateFlow<List<DocumentItem>> = combine(
        documents,
        _selectedMemberId
    ) { docs, memberId ->
        if (memberId == null) emptyList() else docs.filter { it.memberId == memberId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Signature for selected member
    val selectedMemberSignature: StateFlow<MemberSignature?> = combine(
        signatures,
        _selectedMemberId
    ) { sigs, memberId ->
        if (memberId == null) null else sigs.find { it.memberId == memberId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Medical Entries for selected member
    val selectedMemberMedicalEntries: StateFlow<List<MedicalEntry>> = combine(
        medicalEntries,
        _selectedMemberId
    ) { entries, memberId ->
        if (memberId == null) emptyList() else entries.filter { it.memberId == memberId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------------------------------------------------
    // Navigation / Tab Selection
    // -------------------------------------------------------------------------

    fun selectMember(memberId: String?) {
        _selectedMemberId.value = memberId
        _activeMemberTab.value = MemberTab.DOCUMENTS
    }

    fun setActiveMemberTab(tab: MemberTab) {
        _activeMemberTab.value = tab
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            delay(3000)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // -------------------------------------------------------------------------
    // Member Operations
    // -------------------------------------------------------------------------

    fun openAddMemberModal() {
        _editingMember.value = null
        _showAddMemberModal.value = true
    }

    fun openEditMemberModal(member: FamilyMemberProfile) {
        _editingMember.value = member
        _showAddMemberModal.value = true
    }

    fun closeMemberModal() {
        _showAddMemberModal.value = false
        _editingMember.value = null
    }

    fun saveMember(
        id: String?,
        name: String,
        relationship: RelationshipType,
        dob: String,
        bloodGroup: String,
        phone: String,
        email: String,
        avatarColorHex: Long
    ) {
        val initials = name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifBlank { "FM" }

        val generation = when (relationship) {
            RelationshipType.FATHER, RelationshipType.MOTHER, RelationshipType.IN_LAWS -> 1
            RelationshipType.HEAD, RelationshipType.SPOUSE -> 2
            RelationshipType.SON, RelationshipType.DAUGHTER -> 3
            else -> 2
        }

        viewModelScope.launch {
            if (id != null) {
                val existing = familyMembers.value.find { it.id == id }
                if (existing != null) {
                    val updated = existing.copy(
                        name = name,
                        relationship = relationship,
                        relationshipLabel = relationship.label,
                        dob = dob,
                        bloodGroup = bloodGroup,
                        phone = phone,
                        email = email,
                        avatarColorHex = avatarColorHex,
                        avatarInitials = initials,
                        generation = generation
                    )
                    repository.updateFamilyMember(updated)
                    showToast("Updated profile for $name")
                }
            } else {
                val newMember = FamilyMemberProfile(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    relationship = relationship,
                    relationshipLabel = relationship.label,
                    dob = dob,
                    bloodGroup = bloodGroup,
                    phone = phone,
                    email = email,
                    avatarColorHex = avatarColorHex,
                    avatarInitials = initials,
                    generation = generation
                )
                repository.addFamilyMember(newMember)
                showToast("Added family member $name")
            }
        }
        closeMemberModal()
    }

    fun deleteMember(memberId: String) {
        val target = familyMembers.value.find { it.id == memberId }
        viewModelScope.launch {
            repository.deleteFamilyMember(memberId)
            if (_selectedMemberId.value == memberId) {
                _selectedMemberId.value = null
            }
            showToast("Deleted ${target?.name ?: "member"}")
        }
    }

    // -------------------------------------------------------------------------
    // Document Operations
    // -------------------------------------------------------------------------

    fun openAddDocumentModal() {
        _editingDoc.value = null
        _showAddDocModal.value = true
    }

    fun openEditDocumentModal(doc: DocumentItem) {
        _editingDoc.value = doc
        _showAddDocModal.value = true
    }

    fun closeDocumentModal() {
        _showAddDocModal.value = false
        _editingDoc.value = null
    }

    fun setViewingDocument(doc: DocumentItem?) {
        _viewingDoc.value = doc
    }

    fun saveDocument(
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
        filePath: String? = null,
        fileName: String? = null,
        fileSize: String = "1.2 MB",
        fileType: String = "PDF / Image",
        isImage: Boolean = false
    ) {
        val member = familyMembers.value.find { it.id == memberId }
        val memberName = member?.name ?: "Family Member"

        viewModelScope.launch {
            if (id != null) {
                val existing = documents.value.find { it.id == id }
                if (existing != null) {
                    val updated = existing.copy(
                        title = title,
                        documentType = documentType,
                        documentNumber = documentNumber,
                        issuer = issuer,
                        issueDate = issueDate,
                        expiryDate = expiryDate,
                        notes = notes,
                        ocrExtracted = ocrExtracted,
                        filePath = filePath ?: existing.filePath,
                        fileName = fileName ?: existing.fileName,
                        fileSize = if (filePath != null) fileSize else existing.fileSize,
                        fileType = if (filePath != null) fileType else existing.fileType,
                        isImage = if (filePath != null) isImage else existing.isImage
                    )
                    repository.updateDocument(updated)
                    showToast("Updated document: $title")
                }
            } else {
                val newDoc = DocumentItem(
                    id = UUID.randomUUID().toString(),
                    memberId = memberId,
                    memberName = memberName,
                    title = title,
                    documentType = documentType,
                    documentNumber = documentNumber,
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
                repository.addDocument(newDoc)
                showToast("Added document: $title")
            }
        }
        closeDocumentModal()
    }

    fun deleteDocument(docId: String) {
        val target = documents.value.find { it.id == docId }
        viewModelScope.launch {
            repository.deleteDocument(docId)
            if (_viewingDoc.value?.id == docId) {
                _viewingDoc.value = null
            }
            showToast("Deleted document: ${target?.title ?: ""}")
        }
    }

    // -------------------------------------------------------------------------
    // Signature Operations
    // -------------------------------------------------------------------------

    fun openSignatureDialog() {
        _showSignatureDialog.value = true
    }

    fun closeSignatureDialog() {
        _showSignatureDialog.value = false
    }

    fun saveDrawnSignature(memberId: String, strokes: List<List<Offset>>) {
        val member = familyMembers.value.find { it.id == memberId }
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val sig = MemberSignature(
            id = UUID.randomUUID().toString(),
            memberId = memberId,
            signerName = member?.name ?: "Signer",
            createdDate = sdf.format(Date()),
            signatureType = "DRAWN",
            pathPoints = strokes
        )
        viewModelScope.launch {
            repository.saveSignature(sig)
            _showSignatureDialog.value = false
            showToast("Saved digital signature for ${member?.name}")
        }
    }

    fun saveUploadedSignatureImage(memberId: String, imageUri: String) {
        val member = familyMembers.value.find { it.id == memberId }
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val sig = MemberSignature(
            id = UUID.randomUUID().toString(),
            memberId = memberId,
            signerName = member?.name ?: "Signer",
            createdDate = sdf.format(Date()),
            signatureType = "IMAGE",
            imageUri = imageUri
        )
        viewModelScope.launch {
            repository.saveSignature(sig)
            showToast("Uploaded signature photo for ${member?.name}")
        }
    }

    fun deleteSignature(memberId: String) {
        viewModelScope.launch {
            repository.deleteSignatureForMember(memberId)
            showToast("Deleted signature")
        }
    }

    // -------------------------------------------------------------------------
    // Medical History Operations
    // -------------------------------------------------------------------------

    fun openAddMedicalModal() {
        _editingMedicalEntry.value = null
        _showAddMedicalModal.value = true
    }

    fun openEditMedicalModal(entry: MedicalEntry) {
        _editingMedicalEntry.value = entry
        _showAddMedicalModal.value = true
    }

    fun closeMedicalModal() {
        _showAddMedicalModal.value = false
        _editingMedicalEntry.value = null
    }

    fun saveMedicalEntry(
        id: String?,
        memberId: String,
        date: String,
        title: String,
        doctorOrClinic: String,
        notes: String,
        attachedReportName: String?,
        attachedReportPath: String? = null
    ) {
        viewModelScope.launch {
            if (id != null) {
                val existing = medicalEntries.value.find { it.id == id }
                if (existing != null) {
                    val updated = existing.copy(
                        date = date,
                        title = title,
                        doctorOrClinic = doctorOrClinic,
                        notes = notes,
                        attachedReportName = attachedReportName,
                        attachedReportPath = attachedReportPath ?: existing.attachedReportPath
                    )
                    repository.updateMedicalEntry(updated)
                    showToast("Updated medical record")
                }
            } else {
                val newEntry = MedicalEntry(
                    id = UUID.randomUUID().toString(),
                    memberId = memberId,
                    date = date,
                    title = title,
                    doctorOrClinic = doctorOrClinic,
                    notes = notes,
                    attachedReportName = attachedReportName,
                    attachedReportPath = attachedReportPath
                )
                repository.addMedicalEntry(newEntry)
                showToast("Added medical consultation record")
            }
        }
        closeMedicalModal()
    }

    fun deleteMedicalEntry(entryId: String) {
        viewModelScope.launch {
            repository.deleteMedicalEntry(entryId)
            showToast("Deleted medical record")
        }
    }

    fun updateMemberBaselineMedical(
        memberId: String,
        bloodGroup: String,
        allergies: String,
        chronicConditions: String,
        currentMedications: String,
        pastIllnesses: String,
        doctorNotes: String
    ) {
        viewModelScope.launch {
            repository.updateMemberBaselineMedical(
                memberId = memberId,
                bloodGroup = bloodGroup,
                allergies = allergies,
                chronicConditions = chronicConditions,
                currentMedications = currentMedications,
                pastIllnesses = pastIllnesses,
                doctorNotes = doctorNotes
            )
            showToast("Updated baseline medical information")
        }
    }

    // -------------------------------------------------------------------------
    // Family Tree Modal & Google Drive Backup Modal
    // -------------------------------------------------------------------------

    fun setShowFamilyTreeModal(show: Boolean) {
        _showFamilyTreeModal.value = show
    }

    fun setShowDriveBackupModal(show: Boolean) {
        _showDriveBackupModal.value = show
    }

    fun performGoogleDriveBackup() {
        viewModelScope.launch {
            _isBackingUp.value = true
            val result = repository.performDriveBackup()
            _isBackingUp.value = false
            if (result.isSuccess) {
                val backup = result.getOrThrow()
                showToast("Backed up successfully to Google Drive AppData (${backup.fileSize})")
            } else {
                showToast("Backup failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
            }
        }
    }

    fun restoreFromDriveBackup(backup: DriveBackupInfo) {
        viewModelScope.launch {
            _isBackingUp.value = true
            val result = repository.restoreFromBackup(backup)
            _isBackingUp.value = false
            if (result.isSuccess) {
                showToast("Restored vault data from Google Drive: ${backup.formattedDate}")
            } else {
                showToast("Restoration failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
            }
        }
    }

    fun deleteDriveBackup(backupId: String) {
        viewModelScope.launch {
            repository.deleteBackup(backupId)
            showToast("Removed backup file")
        }
    }

    // -------------------------------------------------------------------------
    // PDF Export Actions
    // -------------------------------------------------------------------------

    fun exportMemberCompletePdf(context: Context, member: FamilyMemberProfile) {
        val memberDocs = documents.value.filter { it.memberId == member.id }
        val memberSig = signatures.value.find { it.memberId == member.id }
        val memberMeds = medicalEntries.value.filter { it.memberId == member.id }
        val pdfFile = PdfExportHelper.exportMemberCompletePdf(
            context = context,
            member = member,
            documents = memberDocs,
            signature = memberSig,
            medicalEntries = memberMeds
        )
        PdfExportHelper.shareOrOpenPdf(context, pdfFile)
        showToast("Exported PDF for ${member.name}")
    }

    fun exportDocumentPdf(context: Context, doc: DocumentItem) {
        val member = familyMembers.value.find { it.id == doc.memberId }
            ?: FamilyMemberProfile(name = doc.memberName)
        val pdfFile = PdfExportHelper.exportSingleDocumentPdf(context, member, doc)
        PdfExportHelper.shareOrOpenPdf(context, pdfFile)
        showToast("Exported Document PDF")
    }

    fun exportSignaturePdf(context: Context, signature: MemberSignature) {
        val member = familyMembers.value.find { it.id == signature.memberId }
            ?: FamilyMemberProfile(name = signature.signerName)
        val pdfFile = PdfExportHelper.exportSingleSignaturePdf(context, member, signature)
        PdfExportHelper.shareOrOpenPdf(context, pdfFile)
        showToast("Exported Signature PDF")
    }

    fun exportMedicalEntryPdf(context: Context, entry: MedicalEntry) {
        val member = familyMembers.value.find { it.id == entry.memberId }
            ?: FamilyMemberProfile(name = "Family Member")
        val pdfFile = PdfExportHelper.exportSingleMedicalEntryPdf(context, member, entry)
        PdfExportHelper.shareOrOpenPdf(context, pdfFile)
        showToast("Exported Medical Record PDF")
    }
}
