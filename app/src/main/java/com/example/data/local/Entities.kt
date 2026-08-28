package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.DocumentItem
import com.example.data.DriveBackupInfo
import com.example.data.FamilyMemberProfile
import com.example.data.MedicalEntry
import com.example.data.MemberSignature
import com.example.data.RelationshipType
import androidx.compose.ui.geometry.Offset
import com.squareup.moshi.JsonClass

@Entity(tableName = "family_members")
@JsonClass(generateAdapter = true)
data class MemberEntity(
    @PrimaryKey val id: String,
    val name: String,
    val relationship: String,
    val relationshipLabel: String,
    val dob: String,
    val bloodGroup: String,
    val phone: String,
    val email: String,
    val avatarColorHex: Long,
    val avatarInitials: String,
    val generation: Int,
    val allergies: String,
    val chronicConditions: String,
    val currentMedications: String,
    val pastIllnessesOrSurgeries: String,
    val doctorNotes: String
) {
    fun toDomain(): FamilyMemberProfile {
        val relType = try {
            RelationshipType.valueOf(relationship)
        } catch (_: Exception) {
            RelationshipType.OTHER
        }
        return FamilyMemberProfile(
            id = id,
            name = name,
            relationship = relType,
            relationshipLabel = relationshipLabel,
            dob = dob,
            bloodGroup = bloodGroup,
            phone = phone,
            email = email,
            avatarColorHex = avatarColorHex,
            avatarInitials = avatarInitials,
            generation = generation,
            allergies = allergies,
            chronicConditions = chronicConditions,
            currentMedications = currentMedications,
            pastIllnessesOrSurgeries = pastIllnessesOrSurgeries,
            doctorNotes = doctorNotes
        )
    }

    companion object {
        fun fromDomain(domain: FamilyMemberProfile): MemberEntity {
            return MemberEntity(
                id = domain.id,
                name = domain.name,
                relationship = domain.relationship.name,
                relationshipLabel = domain.relationshipLabel,
                dob = domain.dob,
                bloodGroup = domain.bloodGroup,
                phone = domain.phone,
                email = domain.email,
                avatarColorHex = domain.avatarColorHex,
                avatarInitials = domain.avatarInitials,
                generation = domain.generation,
                allergies = domain.allergies,
                chronicConditions = domain.chronicConditions,
                currentMedications = domain.currentMedications,
                pastIllnessesOrSurgeries = domain.pastIllnessesOrSurgeries,
                doctorNotes = domain.doctorNotes
            )
        }
    }
}

@Entity(tableName = "documents")
@JsonClass(generateAdapter = true)
data class DocumentEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val memberName: String,
    val title: String,
    val documentType: String,
    val documentNumber: String,
    val issuer: String,
    val issueDate: String,
    val expiryDate: String?,
    val notes: String,
    val fileSize: String,
    val fileType: String,
    val ocrExtracted: Boolean,
    val filePath: String? = null,
    val fileName: String? = null,
    val isImage: Boolean = false
) {
    fun toDomain(): DocumentItem {
        return DocumentItem(
            id = id,
            memberId = memberId,
            memberName = memberName,
            title = title,
            documentType = documentType,
            documentNumber = documentNumber,
            issuer = issuer,
            issueDate = issueDate,
            expiryDate = expiryDate,
            notes = notes,
            fileSize = fileSize,
            fileType = fileType,
            ocrExtracted = ocrExtracted,
            filePath = filePath,
            fileName = fileName,
            isImage = isImage
        )
    }

    companion object {
        fun fromDomain(domain: DocumentItem): DocumentEntity {
            return DocumentEntity(
                id = domain.id,
                memberId = domain.memberId,
                memberName = domain.memberName,
                title = domain.title,
                documentType = domain.documentType,
                documentNumber = domain.documentNumber,
                issuer = domain.issuer,
                issueDate = domain.issueDate,
                expiryDate = domain.expiryDate,
                notes = domain.notes,
                fileSize = domain.fileSize,
                fileType = domain.fileType,
                ocrExtracted = domain.ocrExtracted,
                filePath = domain.filePath,
                fileName = domain.fileName,
                isImage = domain.isImage
            )
        }
    }
}

@Entity(tableName = "signatures")
@JsonClass(generateAdapter = true)
data class SignatureEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val signerName: String,
    val createdDate: String,
    val signatureType: String,
    val serializedPoints: String, // format "x,y;x,y|x,y;x,y"
    val imageUri: String?,
    val certificateTag: String
) {
    fun toDomain(): MemberSignature {
        val points = if (serializedPoints.isBlank()) {
            emptyList()
        } else {
            serializedPoints.split("|").filter { it.isNotBlank() }.map { strokeStr ->
                strokeStr.split(";").filter { it.isNotBlank() }.mapNotNull { ptStr ->
                    val coords = ptStr.split(",")
                    if (coords.size == 2) {
                        val x = coords[0].toFloatOrNull()
                        val y = coords[1].toFloatOrNull()
                        if (x != null && y != null) Offset(x, y) else null
                    } else null
                }
            }
        }
        return MemberSignature(
            id = id,
            memberId = memberId,
            signerName = signerName,
            createdDate = createdDate,
            signatureType = signatureType,
            pathPoints = points,
            imageUri = imageUri,
            certificateTag = certificateTag
        )
    }

    companion object {
        fun fromDomain(domain: MemberSignature): SignatureEntity {
            val serialized = domain.pathPoints.joinToString("|") { stroke ->
                stroke.joinToString(";") { "${it.x},${it.y}" }
            }
            return SignatureEntity(
                id = domain.id,
                memberId = domain.memberId,
                signerName = domain.signerName,
                createdDate = domain.createdDate,
                signatureType = domain.signatureType,
                serializedPoints = serialized,
                imageUri = domain.imageUri,
                certificateTag = domain.certificateTag
            )
        }
    }
}

@Entity(tableName = "medical_entries")
@JsonClass(generateAdapter = true)
data class MedicalEntryEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val date: String,
    val title: String,
    val doctorOrClinic: String,
    val notes: String,
    val attachedReportName: String?,
    val attachedReportPath: String? = null
) {
    fun toDomain(): MedicalEntry {
        return MedicalEntry(
            id = id,
            memberId = memberId,
            date = date,
            title = title,
            doctorOrClinic = doctorOrClinic,
            notes = notes,
            attachedReportName = attachedReportName,
            attachedReportPath = attachedReportPath
        )
    }

    companion object {
        fun fromDomain(domain: MedicalEntry): MedicalEntryEntity {
            return MedicalEntryEntity(
                id = domain.id,
                memberId = domain.memberId,
                date = domain.date,
                title = domain.title,
                doctorOrClinic = domain.doctorOrClinic,
                notes = domain.notes,
                attachedReportName = domain.attachedReportName,
                attachedReportPath = domain.attachedReportPath
            )
        }
    }
}

@Entity(tableName = "drive_backups")
@JsonClass(generateAdapter = true)
data class DriveBackupEntity(
    @PrimaryKey val id: String,
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
) {
    fun toDomain(): DriveBackupInfo {
        return DriveBackupInfo(
            id = id,
            backupName = backupName,
            timestamp = timestamp,
            formattedDate = formattedDate,
            memberCount = memberCount,
            documentCount = documentCount,
            medicalCount = medicalCount,
            signatureCount = signatureCount,
            fileSize = fileSize,
            driveFileId = driveFileId,
            jsonData = jsonData
        )
    }

    companion object {
        fun fromDomain(domain: DriveBackupInfo): DriveBackupEntity {
            return DriveBackupEntity(
                id = domain.id,
                backupName = domain.backupName,
                timestamp = domain.timestamp,
                formattedDate = domain.formattedDate,
                memberCount = domain.memberCount,
                documentCount = domain.documentCount,
                medicalCount = domain.medicalCount,
                signatureCount = domain.signatureCount,
                fileSize = domain.fileSize,
                driveFileId = domain.driveFileId,
                jsonData = domain.jsonData
            )
        }
    }
}
