package com.example.data

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import java.util.UUID

data class SavedFileInfo(
    val filePath: String,
    val fileName: String,
    val fileSizeFormatted: String,
    val mimeType: String,
    val isImage: Boolean
)

object FileStorageHelper {
    private const val TAG = "FileStorageHelper"

    fun getDocumentsDir(context: Context): File {
        val dir = File(context.filesDir, "vault_documents")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getSignaturesDir(context: Context): File {
        val dir = File(context.filesDir, "vault_signatures")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getMedicalReportsDir(context: Context): File {
        val dir = File(context.filesDir, "vault_medical_reports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getExportsDir(context: Context): File {
        val dir = File(context.cacheDir, "custodia_exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Creates a temp file and content URI for camera photography.
     */
    fun createTempCameraUri(context: Context): Pair<Uri, File> {
        val cacheDir = File(context.cacheDir, "camera_captures")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val tempFile = File(cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        if (!tempFile.exists()) {
            tempFile.createNewFile()
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        return Pair(uri, tempFile)
    }

    /**
     * Copies any selected URI (from File Manager, Gallery, etc.) to internal vault storage.
     */
    fun saveUriToVault(context: Context, sourceUri: Uri, targetFolder: File): SavedFileInfo? {
        return try {
            val contentResolver = context.contentResolver
            var displayName = "document_${System.currentTimeMillis()}"
            var sizeBytes = 0L

            contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) displayName = cursor.getString(nameIndex) ?: displayName
                    if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
                }
            }

            val mimeType = contentResolver.getType(sourceUri) ?: inferMimeType(displayName)
            val extension = getExtension(displayName, mimeType)
            val safeName = displayName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val uniqueFileName = "${UUID.randomUUID().toString().take(8)}_$safeName"
            val destFile = File(targetFolder, uniqueFileName)

            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val actualSize = destFile.length()
            val sizeFormatted = formatFileSize(actualSize)
            val isImage = mimeType.startsWith("image/") || extension in listOf("jpg", "jpeg", "png", "webp")

            SavedFileInfo(
                filePath = destFile.absolutePath,
                fileName = displayName,
                fileSizeFormatted = sizeFormatted,
                mimeType = mimeType,
                isImage = isImage
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error saving URI to vault", e)
            null
        }
    }

    fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024f
        return when {
            kb >= 1024 -> String.format(Locale.US, "%.1f MB", kb / 1024f)
            kb > 0 -> "${kb.toInt().coerceAtLeast(1)} KB"
            else -> "0 KB"
        }
    }

    fun inferMimeType(fileName: String): String {
        val lower = fileName.lowercase()
        return when {
            lower.endsWith(".pdf") -> "application/pdf"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".webp") -> "image/webp"
            lower.endsWith(".doc") || lower.endsWith(".docx") -> "application/msword"
            lower.endsWith(".txt") -> "text/plain"
            else -> "application/octet-stream"
        }
    }

    private fun getExtension(fileName: String, mimeType: String): String {
        if (fileName.contains(".")) {
            return fileName.substringAfterLast(".").lowercase()
        }
        return when (mimeType) {
            "application/pdf" -> "pdf"
            "image/png" -> "png"
            "image/jpeg" -> "jpg"
            "image/webp" -> "webp"
            else -> "dat"
        }
    }

    /**
     * Shares the real document file (PDF as PDF, Image as Image) via Android system share sheet.
     * If no physical file was uploaded, seamlessly generates and shares the official PDF summary!
     */
    fun shareDocument(
        context: Context,
        document: DocumentItem,
        member: FamilyMemberProfile? = null
    ) {
        try {
            if (document.filePath != null && File(document.filePath).exists()) {
                val file = File(document.filePath)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val mimeType = inferMimeType(document.fileName ?: file.name)

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = ClipData.newRawUri(document.title, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "${document.title} - ${document.memberName}")
                    putExtra(Intent.EXTRA_TEXT, "Shared from Custodia Vault: ${document.title} (${document.documentType} - ${document.documentNumber})")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, "Share Document (${if (mimeType == "application/pdf") "PDF" else "Image"})").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } else {
                // Generate official PDF Dossier and share
                val resolvedMember = member ?: FamilyMemberProfile(name = document.memberName)
                val generatedPdf = PdfExportHelper.exportSingleDocumentPdf(context, resolvedMember, document)
                PdfExportHelper.shareOrOpenPdf(context, generatedPdf)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share document: ${document.title}", e)
            Toast.makeText(context, "Error sharing document: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Saves / Downloads a document or report to the device's public Downloads directory.
     */
    fun downloadDocumentToDevice(
        context: Context,
        document: DocumentItem,
        member: FamilyMemberProfile? = null
    ) {
        try {
            val sourceFile = if (document.filePath != null && File(document.filePath).exists()) {
                File(document.filePath)
            } else {
                val resolvedMember = member ?: FamilyMemberProfile(name = document.memberName)
                PdfExportHelper.exportSingleDocumentPdf(context, resolvedMember, document)
            }

            val targetName = (document.fileName ?: "${document.title.replace(" ", "_")}_${document.documentNumber.take(6)}.pdf")
            val result = saveFileToPublicDownloads(context, sourceFile, targetName, inferMimeType(targetName))

            if (result) {
                Toast.makeText(context, "Downloaded to device: $targetName", Toast.LENGTH_LONG).show()
            } else {
                // Fallback share / save
                shareDocument(context, document, member)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download error", e)
            Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares a Signature as an image PNG or as an official PDF certificate.
     */
    fun shareSignature(
        context: Context,
        signature: MemberSignature,
        member: FamilyMemberProfile? = null,
        asPdf: Boolean = false
    ) {
        try {
            if (asPdf) {
                val resolvedMember = member ?: FamilyMemberProfile(name = signature.signerName)
                val pdfFile = PdfExportHelper.exportSingleSignaturePdf(context, resolvedMember, signature)
                PdfExportHelper.shareOrOpenPdf(context, pdfFile)
            } else {
                val sigFile = createSignatureImageFile(context, signature)
                if (sigFile != null && sigFile.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        sigFile
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        clipData = ClipData.newRawUri(signature.signerName, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Signature - ${signature.signerName}")
                        putExtra(Intent.EXTRA_TEXT, "Digital Signature Specimen for ${signature.signerName} • Custodia Vault (${signature.certificateTag})")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(shareIntent, "Share Signature").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                } else {
                    Toast.makeText(context, "Could not generate signature image", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing signature", e)
            Toast.makeText(context, "Error sharing signature: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Downloads Signature Image or PDF to device storage.
     */
    fun downloadSignatureToDevice(
        context: Context,
        signature: MemberSignature,
        member: FamilyMemberProfile? = null,
        asPdf: Boolean = false
    ) {
        try {
            val sourceFile = if (asPdf) {
                val resolvedMember = member ?: FamilyMemberProfile(name = signature.signerName)
                PdfExportHelper.exportSingleSignaturePdf(context, resolvedMember, signature)
            } else {
                createSignatureImageFile(context, signature)
            }

            if (sourceFile != null && sourceFile.exists()) {
                val targetName = if (asPdf) {
                    "Signature_${signature.signerName.replace(" ", "_")}.pdf"
                } else {
                    "Signature_${signature.signerName.replace(" ", "_")}.png"
                }
                val mime = if (asPdf) "application/pdf" else "image/png"
                val saved = saveFileToPublicDownloads(context, sourceFile, targetName, mime)
                if (saved) {
                    Toast.makeText(context, "Downloaded signature to device: $targetName", Toast.LENGTH_LONG).show()
                } else {
                    shareSignature(context, signature, member, asPdf)
                }
            } else {
                Toast.makeText(context, "Could not prepare signature file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download signature error", e)
            Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares a Medical Entry attached report or summary PDF.
     */
    fun shareMedicalEntry(
        context: Context,
        entry: MedicalEntry,
        member: FamilyMemberProfile? = null
    ) {
        try {
            if (entry.attachedReportPath != null && File(entry.attachedReportPath).exists()) {
                val file = File(entry.attachedReportPath)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val mimeType = inferMimeType(entry.attachedReportName ?: file.name)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = ClipData.newRawUri(entry.title, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Medical Report: ${entry.title}")
                    putExtra(Intent.EXTRA_TEXT, "Medical Consultation Record: ${entry.title} (${entry.date}) • Doctor: ${entry.doctorOrClinic}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, "Share Medical Report").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } else {
                val resolvedMember = member ?: FamilyMemberProfile(name = "Family Member")
                val pdfFile = PdfExportHelper.exportSingleMedicalEntryPdf(context, resolvedMember, entry)
                PdfExportHelper.shareOrOpenPdf(context, pdfFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing medical entry", e)
            Toast.makeText(context, "Error sharing: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Downloads Medical Entry attached report or summary PDF to device.
     */
    fun downloadMedicalEntryToDevice(
        context: Context,
        entry: MedicalEntry,
        member: FamilyMemberProfile? = null
    ) {
        try {
            val sourceFile = if (entry.attachedReportPath != null && File(entry.attachedReportPath).exists()) {
                File(entry.attachedReportPath)
            } else {
                val resolvedMember = member ?: FamilyMemberProfile(name = "Family Member")
                PdfExportHelper.exportSingleMedicalEntryPdf(context, resolvedMember, entry)
            }

            val targetName = entry.attachedReportName ?: "Medical_${entry.title.replace(" ", "_")}.pdf"
            val mime = inferMimeType(targetName)
            val saved = saveFileToPublicDownloads(context, sourceFile, targetName, mime)
            if (saved) {
                Toast.makeText(context, "Downloaded medical record: $targetName", Toast.LENGTH_LONG).show()
            } else {
                shareMedicalEntry(context, entry, member)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download medical error", e)
            Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Renders a digital signature into a clean PNG file for sharing & downloading.
     */
    fun createSignatureImageFile(context: Context, signature: MemberSignature): File? {
        return try {
            if (!signature.imageUri.isNullOrBlank()) {
                val f = File(signature.imageUri)
                if (f.exists()) return f
            }

            val width = 600
            val height = 300
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            val paint = Paint().apply {
                color = Color.parseColor("#1D4ED8") // Blue ink
                style = Paint.Style.STROKE
                strokeWidth = 5f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }

            if (signature.pathPoints.isNotEmpty()) {
                for (stroke in signature.pathPoints) {
                    if (stroke.isNotEmpty()) {
                        val path = Path()
                        path.moveTo(stroke.first().x, stroke.first().y)
                        for (i in 1 until stroke.size) {
                            path.lineTo(stroke[i].x, stroke[i].y)
                        }
                        canvas.drawPath(path, paint)
                    }
                }
            } else {
                val textPaint = Paint().apply {
                    color = Color.parseColor("#1D4ED8")
                    textSize = 36f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.drawText("✍️ ${signature.signerName}", width / 2f, height / 2f + 12f, textPaint)
            }

            val sigFolder = getSignaturesDir(context)
            val outFile = File(sigFolder, "sig_export_${signature.id.take(8)}.png")
            FileOutputStream(outFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            outFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create signature file", e)
            null
        }
    }

    /**
     * Helper to write any file to Public Downloads or App Storage directory.
     */
    private fun saveFileToPublicDownloads(context: Context, sourceFile: File, displayName: String, mimeType: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Custodia")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(sourceFile).use { input ->
                            input.copyTo(out)
                        }
                    }
                    true
                } else {
                    false
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val custodiaDir = File(downloadsDir, "Custodia").apply { if (!exists()) mkdirs() }
                val targetFile = File(custodiaDir, displayName)
                sourceFile.copyTo(targetFile, overwrite = true)
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveFileToPublicDownloads failed", e)
            false
        }
    }

    /**
     * Opens file in external viewer.
     */
    fun openFile(context: Context, filePath: String, mimeType: String = "") {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val resolvedMime = if (mimeType.isNotBlank()) mimeType else inferMimeType(file.name)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, resolvedMime)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open file: $filePath", e)
            Toast.makeText(context, "No app available to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Renders a PDF page to a Bitmap for in-app preview.
     */
    fun renderPdfPageToBitmap(filePath: String, pageIndex: Int = 0): Pair<Bitmap?, Int> {
        return try {
            val file = File(filePath)
            if (!file.exists()) return Pair(null, 0)

            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val pageCount = renderer.pageCount

            if (pageCount == 0) {
                renderer.close()
                pfd.close()
                return Pair(null, 0)
            }

            val clampedIndex = pageIndex.coerceIn(0, pageCount - 1)
            val page = renderer.openPage(clampedIndex)

            val densityScale = 2
            val bitmapWidth = page.width * densityScale
            val bitmapHeight = page.height * densityScale
            val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()
            renderer.close()
            pfd.close()

            Pair(bitmap, pageCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering PDF page in-app", e)
            Pair(null, 0)
        }
    }
}
