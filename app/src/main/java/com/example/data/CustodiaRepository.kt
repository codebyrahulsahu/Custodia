package com.example.data

import android.content.Context
import com.example.data.drive.GoogleDriveManager
import com.example.data.local.CustodiaDao
import com.example.data.local.CustodiaDatabase
import com.example.data.local.DocumentEntity
import com.example.data.local.DriveBackupEntity
import com.example.data.local.MedicalEntryEntity
import com.example.data.local.MemberEntity
import com.example.data.local.SignatureEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustodiaRepository(
    private val context: Context,
    private val database: CustodiaDatabase = CustodiaDatabase.getDatabase(context)
) {
    private val dao: CustodiaDao = database.custodiaDao()
    private val driveManager = GoogleDriveManager(context, dao)

    val familyMembers: Flow<List<FamilyMemberProfile>> = dao.getAllMembers().map { list ->
        list.map { it.toDomain() }
    }

    val documents: Flow<List<DocumentItem>> = dao.getAllDocuments().map { list ->
        list.map { it.toDomain() }
    }

    val signatures: Flow<List<MemberSignature>> = dao.getAllSignatures().map { list ->
        list.map { it.toDomain() }
    }

    val medicalEntries: Flow<List<MedicalEntry>> = dao.getAllMedicalEntries().map { list ->
        list.map { it.toDomain() }
    }

    val driveBackups: Flow<List<DriveBackupInfo>> = dao.getAllBackups().map { list ->
        list.map { it.toDomain() }
    }

    private val _driveAccount = MutableStateFlow(
        DriveAccountInfo(
            email = "kanhaiyalaljojawar@gmail.com",
            displayName = "Kanhaiya Lal",
            isConnected = true,
            appFolder = "appDataFolder (Custodia Vault)",
            lastBackupTime = "Not backed up yet"
        )
    )
    val driveAccount: StateFlow<DriveAccountInfo> = _driveAccount.asStateFlow()

    init {
        // Clean start: No dummy family data seeded. The user starts with an empty, fully functional vault!
    }

    // -------------------------------------------------------------------------
    // Clear Vault
    // -------------------------------------------------------------------------
    suspend fun clearAllVaultData() = withContext(Dispatchers.IO) {
        dao.clearMembers()
        dao.clearDocuments()
        dao.clearSignatures()
        dao.clearMedicalEntries()
    }

    // -------------------------------------------------------------------------
    // Family Member CRUD
    // -------------------------------------------------------------------------

    suspend fun addFamilyMember(member: FamilyMemberProfile) = withContext(Dispatchers.IO) {
        dao.insertMember(MemberEntity.fromDomain(member))
    }

    suspend fun updateFamilyMember(member: FamilyMemberProfile) = withContext(Dispatchers.IO) {
        dao.updateMember(MemberEntity.fromDomain(member))
    }

    suspend fun deleteFamilyMember(id: String) = withContext(Dispatchers.IO) {
        dao.deleteMemberById(id)
        dao.deleteDocumentsByMemberId(id)
        dao.deleteSignatureByMemberId(id)
        dao.deleteMedicalEntriesByMemberId(id)
    }

    // -------------------------------------------------------------------------
    // Document CRUD
    // -------------------------------------------------------------------------

    suspend fun addDocument(document: DocumentItem) = withContext(Dispatchers.IO) {
        dao.insertDocument(DocumentEntity.fromDomain(document))
    }

    suspend fun updateDocument(document: DocumentItem) = withContext(Dispatchers.IO) {
        dao.updateDocument(DocumentEntity.fromDomain(document))
    }

    suspend fun deleteDocument(id: String) = withContext(Dispatchers.IO) {
        dao.deleteDocumentById(id)
    }

    // -------------------------------------------------------------------------
    // Signature CRUD (one per member)
    // -------------------------------------------------------------------------

    suspend fun saveSignature(signature: MemberSignature) = withContext(Dispatchers.IO) {
        dao.deleteSignatureByMemberId(signature.memberId)
        dao.insertSignature(SignatureEntity.fromDomain(signature))
    }

    suspend fun deleteSignatureForMember(memberId: String) = withContext(Dispatchers.IO) {
        dao.deleteSignatureByMemberId(memberId)
    }

    // -------------------------------------------------------------------------
    // Medical Entries CRUD
    // -------------------------------------------------------------------------

    suspend fun addMedicalEntry(entry: MedicalEntry) = withContext(Dispatchers.IO) {
        dao.insertMedicalEntry(MedicalEntryEntity.fromDomain(entry))
    }

    suspend fun updateMedicalEntry(entry: MedicalEntry) = withContext(Dispatchers.IO) {
        dao.updateMedicalEntry(MedicalEntryEntity.fromDomain(entry))
    }

    suspend fun deleteMedicalEntry(id: String) = withContext(Dispatchers.IO) {
        dao.deleteMedicalEntryById(id)
    }

    suspend fun updateMemberBaselineMedical(
        memberId: String,
        bloodGroup: String,
        allergies: String,
        chronicConditions: String,
        currentMedications: String,
        pastIllnesses: String,
        doctorNotes: String
    ) = withContext(Dispatchers.IO) {
        val member = dao.getMemberById(memberId)
        if (member != null) {
            val updated = member.copy(
                bloodGroup = bloodGroup,
                allergies = allergies,
                chronicConditions = chronicConditions,
                currentMedications = currentMedications,
                pastIllnessesOrSurgeries = pastIllnesses,
                doctorNotes = doctorNotes
            )
            dao.updateMember(updated)
        }
    }

    // -------------------------------------------------------------------------
    // Google Drive AppData Backup & Restore
    // -------------------------------------------------------------------------

    suspend fun performDriveBackup(): Result<DriveBackupInfo> {
        val result = driveManager.createAndUploadBackup(_driveAccount.value)
        if (result.isSuccess) {
            val backup = result.getOrThrow()
            _driveAccount.update { it.copy(lastBackupTime = backup.formattedDate) }
        }
        return result
    }

    suspend fun restoreFromBackup(backup: DriveBackupInfo): Result<Unit> {
        val result = driveManager.downloadAndRestoreBackup(backup)
        if (result.isSuccess) {
            val now = System.currentTimeMillis()
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            _driveAccount.update { it.copy(lastBackupTime = "Restored ${sdf.format(Date(now))}") }
        }
        return result
    }

    suspend fun deleteBackup(backupId: String) = withContext(Dispatchers.IO) {
        dao.deleteBackupById(backupId)
    }
}
