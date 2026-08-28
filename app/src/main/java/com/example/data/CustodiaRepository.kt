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
import java.util.UUID

class CustodiaRepository(
    private val context: Context,
    private val database: CustodiaDatabase = CustodiaDatabase.getDatabase(context)
) {
    private val dao: CustodiaDao = database.custodiaDao()
    private val driveManager = GoogleDriveManager(context, dao)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

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
            lastBackupTime = "Just now"
        )
    )
    val driveAccount: StateFlow<DriveAccountInfo> = _driveAccount.asStateFlow()

    init {
        coroutineScope.launch {
            seedInitialDataIfEmpty()
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        val existingMembers = dao.getMembersSnapshot()
        if (existingMembers.isEmpty()) {
            val members = initialFamilyMembers().map { MemberEntity.fromDomain(it) }
            val docs = initialDocuments().map { DocumentEntity.fromDomain(it) }
            val sigs = initialSignatures().map { SignatureEntity.fromDomain(it) }
            val meds = initialMedicalEntries().map { MedicalEntryEntity.fromDomain(it) }

            dao.insertMembers(members)
            dao.insertDocuments(docs)
            dao.insertSignatures(sigs)
            dao.insertMedicalEntries(meds)

            // Seed initial backup in AppData
            driveManager.createAndUploadBackup(_driveAccount.value)
        }
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

    // -------------------------------------------------------------------------
    // Initial Seed Data Helpers
    // -------------------------------------------------------------------------

    private fun initialFamilyMembers(): List<FamilyMemberProfile> {
        return listOf(
            FamilyMemberProfile(
                id = "rajesh",
                name = "Rajesh Sharma",
                relationship = RelationshipType.HEAD,
                relationshipLabel = "Self / Family Head",
                dob = "14 Aug 1982",
                bloodGroup = "B+",
                phone = "+91 98765 43210",
                email = "rajesh.sharma@example.com",
                avatarColorHex = 0xFF0D9488,
                avatarInitials = "RS",
                generation = 2,
                allergies = "Penicillin, Pollen",
                chronicConditions = "Mild Hypertension",
                currentMedications = "Telmisartan 40mg (1 daily)",
                pastIllnessesOrSurgeries = "Appendectomy (2014)",
                doctorNotes = "Regular BP checkups recommended every 3 months."
            ),
            FamilyMemberProfile(
                id = "sunita",
                name = "Sunita Sharma",
                relationship = RelationshipType.SPOUSE,
                relationshipLabel = "Spouse",
                dob = "22 Nov 1986",
                bloodGroup = "O+",
                phone = "+91 98765 43211",
                email = "sunita.sharma@example.com",
                avatarColorHex = 0xFF8B5CF6,
                avatarInitials = "SS",
                generation = 2,
                allergies = "Dust mites, Shellfish",
                chronicConditions = "Hypothyroidism",
                currentMedications = "Thyronorm 50mcg (morning fasting)",
                pastIllnessesOrSurgeries = "None",
                doctorNotes = "Thyroid profile annual review in December."
            ),
            FamilyMemberProfile(
                id = "kamla",
                name = "Kamla Sharma",
                relationship = RelationshipType.MOTHER,
                relationshipLabel = "Mother",
                dob = "10 May 1956",
                bloodGroup = "A+",
                phone = "+91 98765 43212",
                email = "",
                avatarColorHex = 0xFFF59E0B,
                avatarInitials = "KS",
                generation = 1,
                allergies = "Sulfa Drugs",
                chronicConditions = "Type 2 Diabetes, Knee Osteoarthritis",
                currentMedications = "Metformin 500mg (twice daily), Calcium D3",
                pastIllnessesOrSurgeries = "Bilateral Knee Replacement (2021)",
                doctorNotes = "HbA1c target < 6.5%, daily light walking."
            ),
            FamilyMemberProfile(
                id = "aarav",
                name = "Aarav Sharma",
                relationship = RelationshipType.SON,
                relationshipLabel = "Son",
                dob = "18 Sep 2012",
                bloodGroup = "B+",
                phone = "",
                email = "",
                avatarColorHex = 0xFF06B6D4,
                avatarInitials = "AS",
                generation = 3,
                allergies = "Peanuts (Mild)",
                chronicConditions = "None",
                currentMedications = "Multivitamin syrup",
                pastIllnessesOrSurgeries = "None",
                doctorNotes = "Annual pediatric booster completed."
            )
        )
    }

    private fun initialDocuments(): List<DocumentItem> {
        return listOf(
            DocumentItem(
                id = "doc_aadhaar_rajesh",
                memberId = "rajesh",
                memberName = "Rajesh Sharma",
                title = "Aadhaar Card",
                documentType = "Aadhaar Card",
                documentNumber = "4829 7710 3921",
                issuer = "UIDAI, Govt of India",
                issueDate = "12 Jan 2016",
                expiryDate = null,
                notes = "Primary identity & address proof verified via DigiLocker.",
                fileSize = "1.4 MB",
                fileType = "PDF",
                ocrExtracted = true
            ),
            DocumentItem(
                id = "doc_pan_rajesh",
                memberId = "rajesh",
                memberName = "Rajesh Sharma",
                title = "PAN Card",
                documentType = "PAN Card",
                documentNumber = "ABCPS7821K",
                issuer = "Income Tax Dept, Govt of India",
                issueDate = "05 Mar 2011",
                expiryDate = null,
                notes = "Permanent tax identification card linked with Aadhaar.",
                fileSize = "850 KB",
                fileType = "PDF",
                ocrExtracted = true
            ),
            DocumentItem(
                id = "doc_passport_rajesh",
                memberId = "rajesh",
                memberName = "Rajesh Sharma",
                title = "Indian Passport",
                documentType = "Passport",
                documentNumber = "Z8942109",
                issuer = "Ministry of External Affairs, New Delhi",
                issueDate = "15 Sep 2018",
                expiryDate = "14 Sep 2028",
                notes = "36 pages booklet for international travel.",
                fileSize = "2.8 MB",
                fileType = "PDF",
                ocrExtracted = true
            ),
            DocumentItem(
                id = "doc_dl_rajesh",
                memberId = "rajesh",
                memberName = "Rajesh Sharma",
                title = "Driving Licence (LMV + MCWG)",
                documentType = "Driving Licence",
                documentNumber = "DL-0420180092144",
                issuer = "Transport Dept, Delhi NCT",
                issueDate = "22 Jun 2018",
                expiryDate = "21 Jun 2038",
                notes = "Smart card driving licence for car and two-wheeler.",
                fileSize = "1.1 MB",
                fileType = "PDF",
                ocrExtracted = true
            ),
            DocumentItem(
                id = "doc_aadhaar_sunita",
                memberId = "sunita",
                memberName = "Sunita Sharma",
                title = "Aadhaar Card",
                documentType = "Aadhaar Card",
                documentNumber = "7193 8842 1099",
                issuer = "UIDAI, Govt of India",
                issueDate = "18 Feb 2017",
                expiryDate = null,
                notes = "Verified biometric e-Aadhaar.",
                fileSize = "1.3 MB",
                fileType = "PDF",
                ocrExtracted = true
            ),
            DocumentItem(
                id = "doc_pan_sunita",
                memberId = "sunita",
                memberName = "Sunita Sharma",
                title = "PAN Card",
                documentType = "PAN Card",
                documentNumber = "BNMPS4410R",
                issuer = "Income Tax Dept",
                issueDate = "10 Aug 2015",
                expiryDate = null,
                notes = "Permanent Account Number card.",
                fileSize = "780 KB",
                fileType = "PDF",
                ocrExtracted = true
            ),
            DocumentItem(
                id = "doc_birth_aarav",
                memberId = "aarav",
                memberName = "Aarav Sharma",
                title = "Birth Certificate",
                documentType = "Birth Certificate",
                documentNumber = "MCD/2012/99412",
                issuer = "Municipal Corporation of Delhi",
                issueDate = "25 Sep 2012",
                expiryDate = null,
                notes = "Official registered birth certificate for school admissions.",
                fileSize = "1.6 MB",
                fileType = "PDF",
                ocrExtracted = true
            ),
            DocumentItem(
                id = "doc_aadhaar_kamla",
                memberId = "kamla",
                memberName = "Kamla Sharma",
                title = "Senior Citizen Aadhaar Card",
                documentType = "Aadhaar Card",
                documentNumber = "5521 9904 3318",
                issuer = "UIDAI, Govt of India",
                issueDate = "04 Apr 2015",
                expiryDate = null,
                notes = "Senior citizen identification.",
                fileSize = "1.2 MB",
                fileType = "PDF",
                ocrExtracted = true
            )
        )
    }

    private fun initialSignatures(): List<MemberSignature> {
        val samplePointsRajesh = listOf(
            listOf(
                androidx.compose.ui.geometry.Offset(20f, 60f),
                androidx.compose.ui.geometry.Offset(40f, 40f),
                androidx.compose.ui.geometry.Offset(70f, 65f),
                androidx.compose.ui.geometry.Offset(100f, 30f),
                androidx.compose.ui.geometry.Offset(130f, 70f)
            ),
            listOf(
                androidx.compose.ui.geometry.Offset(130f, 70f),
                androidx.compose.ui.geometry.Offset(160f, 35f),
                androidx.compose.ui.geometry.Offset(190f, 60f),
                androidx.compose.ui.geometry.Offset(220f, 25f),
                androidx.compose.ui.geometry.Offset(250f, 80f)
            ),
            listOf(
                androidx.compose.ui.geometry.Offset(40f, 75f),
                androidx.compose.ui.geometry.Offset(240f, 70f)
            )
        )
        val samplePointsSunita = listOf(
            listOf(
                androidx.compose.ui.geometry.Offset(30f, 50f),
                androidx.compose.ui.geometry.Offset(60f, 30f),
                androidx.compose.ui.geometry.Offset(90f, 70f),
                androidx.compose.ui.geometry.Offset(120f, 40f),
                androidx.compose.ui.geometry.Offset(150f, 60f)
            ),
            listOf(
                androidx.compose.ui.geometry.Offset(150f, 60f),
                androidx.compose.ui.geometry.Offset(180f, 35f),
                androidx.compose.ui.geometry.Offset(210f, 55f),
                androidx.compose.ui.geometry.Offset(240f, 30f)
            )
        )

        return listOf(
            MemberSignature(
                id = "sig_rajesh",
                memberId = "rajesh",
                signerName = "Rajesh Sharma",
                createdDate = "12 Jan 2024",
                signatureType = "DRAWN",
                pathPoints = samplePointsRajesh
            ),
            MemberSignature(
                id = "sig_sunita",
                memberId = "sunita",
                signerName = "Sunita Sharma",
                createdDate = "05 Mar 2024",
                signatureType = "DRAWN",
                pathPoints = samplePointsSunita
            )
        )
    }

    private fun initialMedicalEntries(): List<MedicalEntry> {
        return listOf(
            MedicalEntry(
                id = "med_1",
                memberId = "rajesh",
                date = "10 Jan 2024",
                title = "Annual Executive Health Checkup",
                doctorOrClinic = "Dr. S. K. Mehta (Cardiologist, AIIMS)",
                notes = "ECG normal, BP recorded 128/82 mmHg. Advised to continue Telmisartan 40mg and follow low-sodium diet.",
                attachedReportName = "AIIMS_Executive_Health_Report.pdf"
            ),
            MedicalEntry(
                id = "med_2",
                memberId = "rajesh",
                date = "15 Aug 2023",
                title = "Dental Scaling & Root Canal Review",
                doctorOrClinic = "Dr. Priya Dental Care, Saket",
                notes = "Completed deep scaling and polishing. No active cavities detected.",
                attachedReportName = "Dental_Checkup_Receipt.pdf"
            ),
            MedicalEntry(
                id = "med_3",
                memberId = "sunita",
                date = "04 Dec 2023",
                title = "Thyroid Function Test (T3/T4/TSH)",
                doctorOrClinic = "Dr. Lal PathLabs",
                notes = "TSH: 2.4 uIU/mL (well controlled). Dosage of Thyronorm 50mcg confirmed adequate.",
                attachedReportName = "LalPath_Thyroid_Panel.pdf"
            ),
            MedicalEntry(
                id = "med_4",
                memberId = "kamla",
                date = "20 Nov 2023",
                title = "Knee Joint Orthopedic Follow-up",
                doctorOrClinic = "Dr. Ashok Rajgopal (Medanta)",
                notes = "Bilateral knee implants stable, full range of motion achieved. Continue daily quadriceps strengthening.",
                attachedReportName = "Medanta_Orthopedic_Summary.pdf"
            ),
            MedicalEntry(
                id = "med_5",
                memberId = "aarav",
                date = "14 Sep 2023",
                title = "Pediatric Growth & Vision Screening",
                doctorOrClinic = "Max Healthcare Pediatrics",
                notes = "Height: 142 cm, Weight: 34 kg. Vision 6/6 in both eyes. Growth percentiles normal.",
                attachedReportName = "Pediatric_Growth_Chart.pdf"
            )
        )
    }
}
