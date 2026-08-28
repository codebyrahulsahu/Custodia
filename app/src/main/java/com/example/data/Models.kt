package com.example.data

import androidx.compose.ui.geometry.Offset
import java.util.UUID

enum class RelationshipType(val label: String) {
    HEAD("Self / Family Head"),
    SPOUSE("Spouse"),
    MOTHER("Mother"),
    FATHER("Father"),
    SON("Son"),
    DAUGHTER("Daughter"),
    IN_LAWS("In-Laws / Relative"),
    OTHER("Other Dependent")
}

data class FamilyMemberProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val relationship: RelationshipType = RelationshipType.OTHER,
    val relationshipLabel: String = "",
    val dob: String = "01 Jan 1995",
    val bloodGroup: String = "B+",
    val phone: String = "",
    val email: String = "",
    val avatarColorHex: Long = 0xFF0D9488,
    val avatarInitials: String = "FM",
    val generation: Int = 2, // 1: Parents/Seniors, 2: Head/Spouse, 3: Children
    // Medical Baseline Fields
    val allergies: String = "None known",
    val chronicConditions: String = "None",
    val currentMedications: String = "None",
    val pastIllnessesOrSurgeries: String = "None",
    val doctorNotes: String = ""
)

data class DocumentItem(
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val memberName: String,
    val title: String,
    val documentType: String, // Free-text type: e.g. Aadhaar, PAN Card, Passport, Driving License, Degree, Birth Certificate
    val documentNumber: String,
    val issuer: String,
    val issueDate: String,
    val expiryDate: String? = null,
    val notes: String = "",
    val fileSize: String = "1.2 MB",
    val fileType: String = "PDF / Image",
    val ocrExtracted: Boolean = false
)

data class MemberSignature(
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val signerName: String,
    val createdDate: String,
    val signatureType: String = "DRAWN", // "DRAWN" or "IMAGE"
    val pathPoints: List<List<Offset>> = emptyList(),
    val imageUri: String? = null,
    val certificateTag: String = "ECDSA-P256#${UUID.randomUUID().toString().take(8).uppercase()}"
)

data class MedicalEntry(
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val date: String,
    val title: String,
    val doctorOrClinic: String,
    val notes: String,
    val attachedReportName: String? = null
)

data class DriveBackupInfo(
    val id: String = UUID.randomUUID().toString(),
    val backupName: String,
    val timestamp: Long,
    val formattedDate: String,
    val memberCount: Int,
    val documentCount: Int,
    val medicalCount: Int,
    val signatureCount: Int,
    val fileSize: String,
    val driveFileId: String,
    val jsonData: String
)

data class DriveAccountInfo(
    val email: String = "kanhaiyalaljojawar@gmail.com",
    val displayName: String = "Kanhaiya Lal",
    val isConnected: Boolean = true,
    val appFolder: String = "Custodia_Vault_Backups",
    val lastBackupTime: String? = "Today, 10:45 AM"
)
