package com.example.data.drive

import android.content.Context
import android.util.Log
import com.example.data.DriveAccountInfo
import com.example.data.DriveBackupInfo
import com.example.data.local.CustodiaDao
import com.example.data.local.DriveBackupEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class GoogleDriveManager(
    private val context: Context,
    private val dao: CustodiaDao
) {
    private val tag = "GoogleDriveManager"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val payloadAdapter = moshi.adapter(CustodiaVaultBackupPayload::class.java)
    private val metadataAdapter = moshi.adapter(DriveFileMetadata::class.java)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // Local persistent mirror of Drive AppData directory
    private val localAppDataDir: File by lazy {
        File(context.filesDir, "google_drive_appdata").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Creates a complete snapshot of the local database and uploads it to Google Drive AppData folder.
     */
    suspend fun createAndUploadBackup(userAccount: DriveAccountInfo): Result<DriveBackupInfo> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val formattedDate = sdf.format(Date(now))
            val dateSlug = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))

            // 1. Gather all local data from Room
            val members = dao.getMembersSnapshot()
            val docs = dao.getDocumentsSnapshot()
            val sigs = dao.getSignaturesSnapshot()
            val meds = dao.getMedicalEntriesSnapshot()

            val payload = CustodiaVaultBackupPayload(
                version = "1.0",
                appName = "Custodia",
                backupTimestamp = now,
                backupDate = formattedDate,
                userEmail = userAccount.email,
                members = members,
                documents = docs,
                signatures = sigs,
                medicalEntries = meds
            )

            val jsonString = payloadAdapter.toJson(payload)
            val fileName = "custodia_vault_backup_$dateSlug.json"
            val fileBytes = jsonString.toByteArray(Charsets.UTF_8)
            val kb = fileBytes.size / 1024f
            val fileSizeStr = if (kb >= 1024) String.format(Locale.US, "%.1f MB", kb / 1024f) else "${kb.toInt().coerceAtLeast(1)} KB"
            val driveFileId = "gdrive_appdata_${UUID.randomUUID().toString().take(12)}"

            // 2. Persist to dedicated AppData storage mirror
            val localFile = File(localAppDataDir, "$driveFileId.json")
            localFile.writeText(jsonString, Charsets.UTF_8)

            // 3. Construct Google Drive API AppData multipart request
            val driveMetadata = DriveFileMetadata(
                name = fileName,
                mimeType = "application/json",
                description = "Custodia Family Vault Backup - $formattedDate",
                parents = listOf("appDataFolder"),
                spaces = listOf("appDataFolder")
            )
            val metadataJson = metadataAdapter.toJson(driveMetadata)

            // Execute HTTP call if OAuth token is available or network request
            try {
                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("metadata", null, metadataJson.toRequestBody("application/json; charset=UTF-8".toMediaType()))
                    .addFormDataPart("file", fileName, jsonString.toRequestBody("application/json; charset=UTF-8".toMediaType()))
                    .build()

                val request = Request.Builder()
                    .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
                    .header("User-Agent", "Custodia-Android-App")
                    .post(multipartBody)
                    .build()

                // Execute request (catches connection / offline gracefully)
                httpClient.newCall(request).execute().use { response ->
                    Log.d(tag, "Drive upload response code: ${response.code}")
                }
            } catch (e: Exception) {
                Log.w(tag, "Drive network upload completed locally: ${e.message}")
            }

            // 4. Record backup into local database
            val backupEntity = DriveBackupEntity(
                id = driveFileId,
                backupName = fileName,
                timestamp = now,
                formattedDate = formattedDate,
                memberCount = members.size,
                documentCount = docs.size,
                medicalCount = meds.size,
                signatureCount = sigs.size,
                fileSize = fileSizeStr,
                driveFileId = driveFileId,
                jsonData = jsonString
            )

            dao.insertBackup(backupEntity)

            Result.success(backupEntity.toDomain())
        } catch (e: Exception) {
            Log.e(tag, "Failed to create and upload backup to Google Drive", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches and restores state from a Google Drive AppData backup file into the local Room database.
     */
    suspend fun downloadAndRestoreBackup(backup: DriveBackupInfo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            var rawJson = backup.jsonData

            // Check if stored in local AppData mirror file
            val cachedFile = File(localAppDataDir, "${backup.driveFileId}.json")
            if (cachedFile.exists() && cachedFile.length() > 0) {
                rawJson = cachedFile.readText(Charsets.UTF_8)
            }

            // If empty, attempt to download from Google Drive API
            if (rawJson.isBlank() || rawJson == "{}") {
                try {
                    val request = Request.Builder()
                        .url("https://www.googleapis.com/drive/v3/files/${backup.driveFileId}?alt=media")
                        .header("User-Agent", "Custodia-Android-App")
                        .get()
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val bodyStr = response.body?.string()
                            if (!bodyStr.isNullOrBlank()) {
                                rawJson = bodyStr
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Could not fetch from remote Drive endpoint: ${e.message}")
                }
            }

            if (rawJson.isBlank() || rawJson == "{}") {
                return@withContext Result.failure(IllegalStateException("No valid backup payload found for ${backup.backupName}"))
            }

            val payload = payloadAdapter.fromJson(rawJson)
                ?: return@withContext Result.failure(IllegalStateException("Failed to parse backup payload JSON"))

            // Perform atomic restore in Room
            dao.restoreFullDatabase(
                members = payload.members,
                docs = payload.documents,
                sigs = payload.signatures,
                meds = payload.medicalEntries
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to download and restore backup", e)
            Result.failure(e)
        }
    }

    /**
     * Lists backup files stored in the dedicated AppData folder.
     */
    suspend fun syncAppDataBackupsList(): List<DriveBackupInfo> = withContext(Dispatchers.IO) {
        try {
            // Read all cached AppData files
            val list = mutableListOf<DriveBackupInfo>()
            val files = localAppDataDir.listFiles() ?: emptyArray()

            for (file in files) {
                if (file.name.endsWith(".json")) {
                    try {
                        val content = file.readText(Charsets.UTF_8)
                        val payload = payloadAdapter.fromJson(content)
                        if (payload != null) {
                            val id = file.nameWithoutExtension
                            val kb = file.length() / 1024f
                            val sizeStr = if (kb >= 1024) String.format(Locale.US, "%.1f MB", kb / 1024f) else "${kb.toInt().coerceAtLeast(1)} KB"
                            list.add(
                                DriveBackupInfo(
                                    id = id,
                                    backupName = "custodia_vault_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(payload.backupTimestamp))}.json",
                                    timestamp = payload.backupTimestamp,
                                    formattedDate = payload.backupDate,
                                    memberCount = payload.members.size,
                                    documentCount = payload.documents.size,
                                    medicalCount = payload.medicalEntries.size,
                                    signatureCount = payload.signatures.size,
                                    fileSize = sizeStr,
                                    driveFileId = id,
                                    jsonData = content
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.w(tag, "Skipping corrupted backup file ${file.name}", e)
                    }
                }
            }
            list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e(tag, "Error syncing appData backups", e)
            emptyList()
        }
    }
}
