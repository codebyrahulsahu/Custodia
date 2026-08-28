package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportHelper {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN = 40f

    /**
     * Generates a complete comprehensive PDF document for a family member:
     * - Cover section with Name, Relationship, Blood Group, Contact details, DOB
     * - Documents section with list, types, numbers, issuers, dates
     * - Signature section with rendered digital signature strokes
     * - Medical History section with baseline fields & date-stamped medical entries
     */
    fun exportMemberCompletePdf(
        context: Context,
        member: FamilyMemberProfile,
        documents: List<DocumentItem>,
        signature: MemberSignature?,
        medicalEntries: List<MedicalEntry>
    ): File {
        val pdfDoc = PdfDocument()
        var pageNumber = 1

        // ---------------- PAGE 1: Member Profile & Documents ----------------
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas

        // Header Background Banner
        val headerPaint = Paint().apply {
            color = Color.parseColor("#0F172A") // Dark Slate
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 120f, headerPaint)

        // Accent strip
        val accentPaint = Paint().apply {
            color = Color.parseColor("#0D9488") // Teal
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 116f, PAGE_WIDTH.toFloat(), 120f, accentPaint)

        // Title Text
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("CUSTODIA FAMILY VAULT", MARGIN, 45f, titlePaint)

        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val genDate = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("Comprehensive Member Dossier • Exported on $genDate", MARGIN, 65f, subTitlePaint)

        // Member Avatar & Profile Card Box
        var currentY = 140f

        val cardBgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val profileRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 110f)
        canvas.drawRoundRect(profileRect, 8f, 8f, cardBgPaint)
        canvas.drawRoundRect(profileRect, 8f, 8f, cardBorderPaint)

        // Avatar Circle
        val avatarPaint = Paint().apply {
            color = Color.parseColor("#0D9488")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(MARGIN + 45f, currentY + 55f, 35f, avatarPaint)

        val initialsPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(member.avatarInitials, MARGIN + 45f, currentY + 62f, initialsPaint)

        // Member Details
        val memberNamePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(member.name, MARGIN + 95f, currentY + 35f, memberNamePaint)

        val labelPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val valPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Col 1
        canvas.drawText("Relationship:", MARGIN + 95f, currentY + 55f, labelPaint)
        canvas.drawText(member.relationship.label, MARGIN + 165f, currentY + 55f, valPaint)

        canvas.drawText("Date of Birth:", MARGIN + 95f, currentY + 72f, labelPaint)
        canvas.drawText(member.dob, MARGIN + 165f, currentY + 72f, valPaint)

        canvas.drawText("Blood Group:", MARGIN + 95f, currentY + 89f, labelPaint)
        canvas.drawText(member.bloodGroup, MARGIN + 165f, currentY + 89f, valPaint)

        // Col 2
        canvas.drawText("Phone:", MARGIN + 310f, currentY + 55f, labelPaint)
        canvas.drawText(if (member.phone.isNotBlank()) member.phone else "Not specified", MARGIN + 355f, currentY + 55f, valPaint)

        canvas.drawText("Email:", MARGIN + 310f, currentY + 72f, labelPaint)
        canvas.drawText(if (member.email.isNotBlank()) member.email else "Not specified", MARGIN + 355f, currentY + 72f, valPaint)

        currentY += 135f

        // SECTION 1: DOCUMENTS
        val sectionTitlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("1. OFFICIAL DOCUMENTS (${documents.size})", MARGIN, currentY, sectionTitlePaint)

        val dividerPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1.5f
        }
        canvas.drawLine(MARGIN, currentY + 6f, PAGE_WIDTH - MARGIN, currentY + 6f, dividerPaint)
        currentY += 22f

        if (documents.isEmpty()) {
            canvas.drawText("No documents stored for this member.", MARGIN, currentY + 15f, labelPaint)
            currentY += 35f
        } else {
            // Table Header
            val thBg = Paint().apply { color = Color.parseColor("#E2E8F0") }
            canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 22f, thBg)

            val thText = Paint().apply {
                color = Color.parseColor("#334155")
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("DOCUMENT TYPE & TITLE", MARGIN + 8f, currentY + 15f, thText)
            canvas.drawText("DOCUMENT NO.", MARGIN + 210f, currentY + 15f, thText)
            canvas.drawText("ISSUER", MARGIN + 330f, currentY + 15f, thText)
            canvas.drawText("ISSUE / EXPIRY", MARGIN + 435f, currentY + 15f, thText)
            currentY += 25f

            for ((idx, doc) in documents.take(6).withIndex()) {
                val rowBg = if (idx % 2 == 0) Color.WHITE else Color.parseColor("#F8FAFC")
                val rowPaint = Paint().apply { color = rowBg }
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 34f, rowPaint)

                val docTitle = Paint().apply {
                    color = Color.parseColor("#0F172A")
                    textSize = 10f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val docSub = Paint().apply {
                    color = Color.parseColor("#64748B")
                    textSize = 8.5f
                }
                canvas.drawText(doc.title, MARGIN + 8f, currentY + 14f, docTitle)
                canvas.drawText(doc.documentType, MARGIN + 8f, currentY + 26f, docSub)

                val docNumPaint = Paint().apply {
                    color = Color.parseColor("#1E293B")
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                }
                canvas.drawText(doc.documentNumber, MARGIN + 210f, currentY + 18f, docNumPaint)

                val issuerPaint = Paint().apply {
                    color = Color.parseColor("#334155")
                    textSize = 8.5f
                }
                val shortIssuer = if (doc.issuer.length > 18) doc.issuer.take(17) + ".." else doc.issuer
                canvas.drawText(shortIssuer, MARGIN + 330f, currentY + 18f, issuerPaint)

                val datePaint = Paint().apply {
                    color = Color.parseColor("#334155")
                    textSize = 8.5f
                }
                val dateStr = doc.issueDate + (if (doc.expiryDate != null) " - ${doc.expiryDate}" else " (Permanent)")
                canvas.drawText(dateStr, MARGIN + 435f, currentY + 18f, datePaint)

                canvas.drawLine(MARGIN, currentY + 34f, PAGE_WIDTH - MARGIN, currentY + 34f, dividerPaint)
                currentY += 36f
            }
        }

        // SECTION 2: DIGITAL SIGNATURE
        currentY += 15f
        canvas.drawText("2. VERIFIED DIGITAL SIGNATURE", MARGIN, currentY, sectionTitlePaint)
        canvas.drawLine(MARGIN, currentY + 6f, PAGE_WIDTH - MARGIN, currentY + 6f, dividerPaint)
        currentY += 20f

        val sigCardRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 100f)
        canvas.drawRoundRect(sigCardRect, 6f, 6f, cardBgPaint)
        canvas.drawRoundRect(sigCardRect, 6f, 6f, cardBorderPaint)

        if (signature != null && signature.pathPoints.isNotEmpty()) {
            // Draw actual signature strokes
            val strokePaint = Paint().apply {
                color = Color.parseColor("#1E3A8A") // Deep Blue Ink
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }

            canvas.save()
            canvas.translate(MARGIN + 30f, currentY + 20f)
            for (stroke in signature.pathPoints) {
                if (stroke.isNotEmpty()) {
                    val p = Path()
                    p.moveTo(stroke.first().x * 0.7f, stroke.first().y * 0.7f)
                    for (i in 1 until stroke.size) {
                        p.lineTo(stroke[i].x * 0.7f, stroke[i].y * 0.7f)
                    }
                    canvas.drawPath(p, strokePaint)
                }
            }
            canvas.restore()

            // Signature Meta
            canvas.drawText("Signer: ${signature.signerName}", MARGIN + 310f, currentY + 35f, valPaint)
            canvas.drawText("Recorded Date: ${signature.createdDate}", MARGIN + 310f, currentY + 52f, labelPaint)
            canvas.drawText("Security Tag: ${signature.certificateTag}", MARGIN + 310f, currentY + 69f, labelPaint)
        } else {
            val emptySigPaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
            canvas.drawText("No digital signature captured for this member.", MARGIN + 20f, currentY + 50f, emptySigPaint)
        }

        currentY += 120f

        // Footer on Page 1
        drawFooter(canvas, 1, 2)
        pdfDoc.finishPage(page)

        // ---------------- PAGE 2: Medical History ----------------
        pageNumber = 2
        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        page = pdfDoc.startPage(pageInfo)
        canvas = page.canvas

        // Header Banner Page 2
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 60f, headerPaint)
        canvas.drawRect(0f, 57f, PAGE_WIDTH.toFloat(), 60f, accentPaint)
        canvas.drawText("CUSTODIA • MEDICAL HISTORY: ${member.name.uppercase()}", MARGIN, 38f, titlePaint.apply { textSize = 15f })

        currentY = 85f

        // Baseline Health Profile Card
        canvas.drawText("3. BASELINE MEDICAL PROFILE", MARGIN, currentY, sectionTitlePaint)
        canvas.drawLine(MARGIN, currentY + 6f, PAGE_WIDTH - MARGIN, currentY + 6f, dividerPaint)
        currentY += 20f

        val medBoxRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 125f)
        canvas.drawRoundRect(medBoxRect, 6f, 6f, cardBgPaint)
        canvas.drawRoundRect(medBoxRect, 6f, 6f, cardBorderPaint)

        var medY = currentY + 22f
        drawMedField(canvas, "Blood Group:", member.bloodGroup, MARGIN + 15f, medY, labelPaint, valPaint)
        drawMedField(canvas, "Allergies:", member.allergies, MARGIN + 260f, medY, labelPaint, valPaint)

        medY += 22f
        drawMedField(canvas, "Chronic Conditions:", member.chronicConditions, MARGIN + 15f, medY, labelPaint, valPaint)
        drawMedField(canvas, "Current Medications:", member.currentMedications, MARGIN + 260f, medY, labelPaint, valPaint)

        medY += 22f
        drawMedField(canvas, "Past Illnesses/Surgeries:", member.pastIllnessesOrSurgeries, MARGIN + 15f, medY, labelPaint, valPaint)

        medY += 22f
        drawMedField(canvas, "Doctor Notes:", if (member.doctorNotes.isNotBlank()) member.doctorNotes else "None", MARGIN + 15f, medY, labelPaint, valPaint)

        currentY += 150f

        // Medical Entries History Table
        canvas.drawText("4. MEDICAL RECORDS & CONSULTATIONS (${medicalEntries.size})", MARGIN, currentY, sectionTitlePaint)
        canvas.drawLine(MARGIN, currentY + 6f, PAGE_WIDTH - MARGIN, currentY + 6f, dividerPaint)
        currentY += 20f

        if (medicalEntries.isEmpty()) {
            canvas.drawText("No medical consultation records logged.", MARGIN, currentY + 15f, labelPaint)
        } else {
            for (entry in medicalEntries) {
                val entryRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 70f)
                canvas.drawRoundRect(entryRect, 4f, 4f, cardBgPaint)
                canvas.drawRoundRect(entryRect, 4f, 4f, cardBorderPaint)

                val entryTitlePaint = Paint().apply {
                    color = Color.parseColor("#0F172A")
                    textSize = 11f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val dateBadge = Paint().apply {
                    color = Color.parseColor("#0D9488")
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(entry.title, MARGIN + 12f, currentY + 20f, entryTitlePaint)
                canvas.drawText(entry.date, PAGE_WIDTH - MARGIN - 90f, currentY + 20f, dateBadge)

                val docClinic = Paint().apply {
                    color = Color.parseColor("#475569")
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                }
                canvas.drawText("Consultant: ${entry.doctorOrClinic}", MARGIN + 12f, currentY + 36f, docClinic)

                val notesPaint = Paint().apply {
                    color = Color.parseColor("#334155")
                    textSize = 9f
                }
                val shortNotes = if (entry.notes.length > 95) entry.notes.take(92) + "..." else entry.notes
                canvas.drawText(shortNotes, MARGIN + 12f, currentY + 54f, notesPaint)

                currentY += 80f
                if (currentY > PAGE_HEIGHT - 80f) break
            }
        }

        // Footer on Page 2
        drawFooter(canvas, 2, 2)
        pdfDoc.finishPage(page)

        // Save PDF to output file
        val outputDir = File(context.cacheDir, "custodia_exports").apply { mkdirs() }
        val safeName = member.name.replace(" ", "_").lowercase(Locale.US)
        val outputFile = File(outputDir, "Custodia_${safeName}_Dossier.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()
        return outputFile
    }

    /**
     * Exports an individual document as its own PDF.
     */
    fun exportSingleDocumentPdf(
        context: Context,
        member: FamilyMemberProfile,
        doc: DocumentItem
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        // Header
        val headerPaint = Paint().apply { color = Color.parseColor("#0F172A") }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 100f, headerPaint)
        val accentPaint = Paint().apply { color = Color.parseColor("#0D9488") }
        canvas.drawRect(0f, 96f, PAGE_WIDTH.toFloat(), 100f, accentPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("CUSTODIA DOCUMENT RECORD", MARGIN, 45f, titlePaint)
        val subPaint = Paint().apply { color = Color.parseColor("#94A3B8"); textSize = 10.5f }
        canvas.drawText("Official Verified Record • Member: ${member.name}", MARGIN, 65f, subPaint)

        var currentY = 130f
        val boxRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 280f)
        val bgPaint = Paint().apply { color = Color.parseColor("#F8FAFC") }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(boxRect, 8f, 8f, bgPaint)
        canvas.drawRoundRect(boxRect, 8f, 8f, borderPaint)

        val valBig = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(doc.title, MARGIN + 25f, currentY + 45f, valBig)

        val lbl = Paint().apply { color = Color.parseColor("#64748B"); textSize = 11f }
        val v = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        var rowY = currentY + 80f
        drawMedField(canvas, "Document Type:", doc.documentType, MARGIN + 25f, rowY, lbl, v)
        rowY += 30f
        drawMedField(canvas, "Document Number:", doc.documentNumber, MARGIN + 25f, rowY, lbl, v.apply { typeface = Typeface.MONOSPACE })
        rowY += 30f
        drawMedField(canvas, "Issuing Authority:", doc.issuer, MARGIN + 25f, rowY, lbl, v.apply { typeface = Typeface.DEFAULT_BOLD })
        rowY += 30f
        drawMedField(canvas, "Issue Date:", doc.issueDate, MARGIN + 25f, rowY, lbl, v)
        rowY += 30f
        val expStr = doc.expiryDate ?: "Permanent / Non-expiring"
        drawMedField(canvas, "Expiry Date:", expStr, MARGIN + 25f, rowY, lbl, v)
        rowY += 30f
        drawMedField(canvas, "Notes:", if (doc.notes.isNotBlank()) doc.notes else "None", MARGIN + 25f, rowY, lbl, v)

        drawFooter(canvas, 1, 1)
        pdfDoc.finishPage(page)

        val outputDir = File(context.cacheDir, "custodia_exports").apply { mkdirs() }
        val safeDocName = doc.title.replace(" ", "_").lowercase(Locale.US)
        val outputFile = File(outputDir, "Doc_${safeDocName}.pdf")
        FileOutputStream(outputFile).use { out -> pdfDoc.writeTo(out) }
        pdfDoc.close()
        return outputFile
    }

    /**
     * Exports an individual signature as its own PDF.
     */
    fun exportSingleSignaturePdf(
        context: Context,
        member: FamilyMemberProfile,
        signature: MemberSignature
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val headerPaint = Paint().apply { color = Color.parseColor("#0F172A") }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 100f, headerPaint)
        val accentPaint = Paint().apply { color = Color.parseColor("#0D9488") }
        canvas.drawRect(0f, 96f, PAGE_WIDTH.toFloat(), 100f, accentPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("VERIFIED SIGNATURE SPECIMEN", MARGIN, 45f, titlePaint)
        val subPaint = Paint().apply { color = Color.parseColor("#94A3B8"); textSize = 10.5f }
        canvas.drawText("Custodia Digital Vault • Member: ${member.name}", MARGIN, 65f, subPaint)

        val currentY = 140f
        val boxRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 220f)
        val bgPaint = Paint().apply { color = Color.parseColor("#F8FAFC") }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(boxRect, 8f, 8f, bgPaint)
        canvas.drawRoundRect(boxRect, 8f, 8f, borderPaint)

        // Draw Strokes
        val strokePaint = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
        canvas.save()
        canvas.translate(MARGIN + 60f, currentY + 40f)
        for (stroke in signature.pathPoints) {
            if (stroke.isNotEmpty()) {
                val p = Path()
                p.moveTo(stroke.first().x, stroke.first().y)
                for (i in 1 until stroke.size) {
                    p.lineTo(stroke[i].x, stroke[i].y)
                }
                canvas.drawPath(p, strokePaint)
            }
        }
        canvas.restore()

        val lbl = Paint().apply { color = Color.parseColor("#64748B"); textSize = 10.5f }
        val v = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Signer: ${signature.signerName}", MARGIN + 25f, currentY + 175f, v)
        canvas.drawText("Recorded Date: ${signature.createdDate}", MARGIN + 25f, currentY + 195f, lbl)
        canvas.drawText("Certificate ID: ${signature.certificateTag}", MARGIN + 280f, currentY + 195f, lbl)

        drawFooter(canvas, 1, 1)
        pdfDoc.finishPage(page)

        val outputDir = File(context.cacheDir, "custodia_exports").apply { mkdirs() }
        val safeName = member.name.replace(" ", "_").lowercase(Locale.US)
        val outputFile = File(outputDir, "Signature_${safeName}.pdf")
        FileOutputStream(outputFile).use { out -> pdfDoc.writeTo(out) }
        pdfDoc.close()
        return outputFile
    }

    /**
     * Exports a single medical entry as its own PDF.
     */
    fun exportSingleMedicalEntryPdf(
        context: Context,
        member: FamilyMemberProfile,
        entry: MedicalEntry
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val headerPaint = Paint().apply { color = Color.parseColor("#0F172A") }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 100f, headerPaint)
        val accentPaint = Paint().apply { color = Color.parseColor("#0D9488") }
        canvas.drawRect(0f, 96f, PAGE_WIDTH.toFloat(), 100f, accentPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("MEDICAL CONSULTATION RECORD", MARGIN, 45f, titlePaint)
        val subPaint = Paint().apply { color = Color.parseColor("#94A3B8"); textSize = 10.5f }
        canvas.drawText("Patient: ${member.name} • Blood Group: ${member.bloodGroup}", MARGIN, 65f, subPaint)

        val currentY = 130f
        val boxRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 240f)
        val bgPaint = Paint().apply { color = Color.parseColor("#F8FAFC") }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(boxRect, 8f, 8f, bgPaint)
        canvas.drawRoundRect(boxRect, 8f, 8f, borderPaint)

        val titleP = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(entry.title, MARGIN + 25f, currentY + 40f, titleP)

        val lbl = Paint().apply { color = Color.parseColor("#64748B"); textSize = 11f }
        val v = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        var rowY = currentY + 75f
        drawMedField(canvas, "Consultation Date:", entry.date, MARGIN + 25f, rowY, lbl, v)
        rowY += 30f
        drawMedField(canvas, "Doctor / Clinic:", entry.doctorOrClinic, MARGIN + 25f, rowY, lbl, v)
        rowY += 30f
        drawMedField(canvas, "Clinical Notes:", entry.notes, MARGIN + 25f, rowY, lbl, v)
        rowY += 30f
        if (entry.attachedReportName != null) {
            drawMedField(canvas, "Attached Report:", entry.attachedReportName, MARGIN + 25f, rowY, lbl, v)
        }

        drawFooter(canvas, 1, 1)
        pdfDoc.finishPage(page)

        val outputDir = File(context.cacheDir, "custodia_exports").apply { mkdirs() }
        val outputFile = File(outputDir, "Medical_${entry.title.replace(" ", "_").lowercase(Locale.US)}.pdf")
        FileOutputStream(outputFile).use { out -> pdfDoc.writeTo(out) }
        pdfDoc.close()
        return outputFile
    }

    private fun drawMedField(
        canvas: Canvas,
        label: String,
        value: String,
        x: Float,
        y: Float,
        lblPaint: Paint,
        valPaint: Paint
    ) {
        canvas.drawText(label, x, y, lblPaint)
        val textWidth = lblPaint.measureText(label)
        canvas.drawText(value, x + textWidth + 8f, y, valPaint)
    }

    private fun drawFooter(canvas: Canvas, current: Int, total: Int) {
        val footerPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 9f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, PAGE_HEIGHT - 40f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 40f, linePaint)
        canvas.drawText("Custodia Family Vault • Page $current of $total • Confidential", PAGE_WIDTH / 2f, PAGE_HEIGHT - 25f, footerPaint)
    }

    fun shareOrOpenPdf(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = android.content.ClipData.newRawUri(pdfFile.name, uri)
                putExtra(Intent.EXTRA_SUBJECT, pdfFile.name)
                putExtra(Intent.EXTRA_TEXT, "Exported from Custodia Family Vault: ${pdfFile.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share PDF Document").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(android.net.Uri.fromFile(pdfFile), "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }
}
