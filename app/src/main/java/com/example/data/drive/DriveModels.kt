package com.example.data.drive

import com.example.data.local.DocumentEntity
import com.example.data.local.MedicalEntryEntity
import com.example.data.local.MemberEntity
import com.example.data.local.SignatureEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DriveFileMetadata(
    val id: String? = null,
    val name: String,
    val mimeType: String? = "application/json",
    val description: String? = null,
    val size: String? = null,
    val createdTime: String? = null,
    val modifiedTime: String? = null,
    val parents: List<String>? = listOf("appDataFolder"),
    val spaces: List<String>? = listOf("appDataFolder")
)

@JsonClass(generateAdapter = true)
data class DriveFileListResponse(
    val files: List<DriveFileMetadata> = emptyList(),
    val nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class CustodiaVaultBackupPayload(
    val version: String = "1.0",
    val appName: String = "Custodia",
    val backupTimestamp: Long,
    val backupDate: String,
    val userEmail: String,
    val members: List<MemberEntity> = emptyList(),
    val documents: List<DocumentEntity> = emptyList(),
    val signatures: List<SignatureEntity> = emptyList(),
    val medicalEntries: List<MedicalEntryEntity> = emptyList()
)
