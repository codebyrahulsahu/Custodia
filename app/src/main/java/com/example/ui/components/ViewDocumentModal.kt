package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.DocumentItem
import com.example.data.FileStorageHelper
import com.example.ui.theme.BackgroundWhite
import com.example.ui.theme.BlueSoftPill
import com.example.ui.theme.BlueTintBackground
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VaultCardBorder
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceElevated
import com.example.ui.theme.VerifiedGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ViewDocumentModal(
    document: DocumentItem,
    onDismiss: () -> Unit,
    onEditClick: (DocumentItem) -> Unit,
    onDeleteClick: (DocumentItem) -> Unit,
    onDownloadPdfClick: (DocumentItem) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // In-App PDF rendering state
    var pdfPageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pdfTotalPages by remember { mutableIntStateOf(1) }
    var currentPdfPage by remember { mutableIntStateOf(0) }
    var isPdfLoading by remember { mutableStateOf(false) }

    val isPdf = document.filePath != null && (
        document.filePath.endsWith(".pdf", ignoreCase = true) ||
        document.fileName?.endsWith(".pdf", ignoreCase = true) == true ||
        document.fileType.contains("pdf", ignoreCase = true)
    )

    LaunchedEffect(document.filePath, currentPdfPage) {
        if (isPdf && document.filePath != null) {
            isPdfLoading = true
            val result = withContext(Dispatchers.IO) {
                FileStorageHelper.renderPdfPageToBitmap(document.filePath, currentPdfPage)
            }
            pdfPageBitmap = result.first
            pdfTotalPages = result.second.coerceAtLeast(1)
            isPdfLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, VaultCardBorder, RoundedCornerShape(20.dp)),
            color = BackgroundWhite,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BlueSoftPill),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Description,
                                contentDescription = null,
                                tint = RoyalBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = document.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "Member: ${document.memberName} • ${document.documentType}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                // IN-APP DOCUMENT VIEWER (Displays Original PDF / Image inside the app)
                if (document.filePath != null) {
                    val file = File(document.filePath)
                    if (file.exists()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(VaultSurface)
                                .border(1.dp, VaultCardBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Viewer Header & External App Link
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(BlueTintBackground)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isPdf) "IN-APP PDF VIEWER" else "IN-APP IMAGE VIEWER",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalBluePrimary,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                FileStorageHelper.openFile(context, document.filePath)
                                            }
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = "External App",
                                            fontSize = 11.sp,
                                            color = ElectricCyan,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }

                                // Visual Render Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(VaultSurfaceElevated)
                                        .border(1.dp, VaultCardBorder, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPdf) {
                                        if (isPdfLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(32.dp),
                                                color = RoyalBluePrimary
                                            )
                                        } else if (pdfPageBitmap != null) {
                                            Image(
                                                bitmap = pdfPageBitmap!!.asImageBitmap(),
                                                contentDescription = "PDF Page View",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Fit
                                            )
                                        } else {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.PictureAsPdf,
                                                    contentDescription = null,
                                                    tint = RoyalBluePrimary,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                                Text(
                                                    text = "PDF Document Ready",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = document.fileName ?: file.name,
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    } else {
                                        // Image Viewer
                                        AsyncImage(
                                            model = file,
                                            contentDescription = "Original Document Image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }

                                // PDF Page Navigation Controls if multi-page PDF
                                if (isPdf && pdfTotalPages > 1) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { if (currentPdfPage > 0) currentPdfPage-- },
                                            enabled = currentPdfPage > 0,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.NavigateBefore,
                                                contentDescription = "Previous Page",
                                                tint = if (currentPdfPage > 0) RoyalBluePrimary else TextDisabled
                                            )
                                        }

                                        Text(
                                            text = "Page ${currentPdfPage + 1} of $pdfTotalPages",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextSecondary
                                        )

                                        IconButton(
                                            onClick = { if (currentPdfPage < pdfTotalPages - 1) currentPdfPage++ },
                                            enabled = currentPdfPage < pdfTotalPages - 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.NavigateNext,
                                                contentDescription = "Next Page",
                                                tint = if (currentPdfPage < pdfTotalPages - 1) RoyalBluePrimary else TextDisabled
                                            )
                                        }
                                    }
                                }

                                // File Details Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = document.fileName ?: file.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = document.fileSize,
                                        fontSize = 11.5.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Document Metadata Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(VaultSurface)
                        .border(1.dp, VaultCardBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Document Number with Copy
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Numbers,
                                        contentDescription = null,
                                        tint = RoyalBluePrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Document Number",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = document.documentNumber.ifBlank { "Not Specified" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = TextPrimary
                                )
                            }

                            if (document.documentNumber.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(document.documentNumber))
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(BlueTintBackground)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy Number",
                                        tint = RoyalBluePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Divider(color = VaultCardBorder)

                        // Issuing Authority
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = RoyalBluePrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Issuing Authority",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = document.issuer.ifBlank { "Not Specified" },
                                fontSize = 13.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Divider(color = VaultCardBorder)

                        // Dates
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = RoyalBluePrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Issue Date", fontSize = 11.sp, color = TextSecondary)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = document.issueDate, fontSize = 13.sp, color = TextPrimary)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Expiry Date", fontSize = 11.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = document.expiryDate ?: "Permanent (No Expiry)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (document.expiryDate == null) VerifiedGreen else TextPrimary
                                )
                            }
                        }

                        if (document.notes.isNotBlank()) {
                            Divider(color = VaultCardBorder)
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Notes,
                                        contentDescription = null,
                                        tint = RoyalBluePrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Notes / Remarks", fontSize = 11.sp, color = TextSecondary)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = document.notes,
                                    fontSize = 12.5.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // ACTION BUTTONS (Share Original Document, Download PDF, Edit, Delete)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Primary Row: Share Original File & PDF Export
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Share Original Document (PDF as PDF, Image as Image)
                        Button(
                            onClick = {
                                FileStorageHelper.shareOriginalFile(
                                    context = context,
                                    filePath = document.filePath,
                                    originalFileName = document.fileName,
                                    title = "${document.title} - ${document.memberName}"
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("btn_share_original_document")
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPdf) "Share Original PDF" else "Share Original",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.5.sp
                            )
                        }

                        // Export Summary PDF
                        Button(
                            onClick = {
                                onDownloadPdfClick(document)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueSoftPill),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_modal_download_pdf")
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                tint = RoyalBlueDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Export PDF",
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlueDark,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Secondary Row: Edit & Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onDismiss()
                                onEditClick(document)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VaultSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VaultCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = RoyalBluePrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", color = RoyalBluePrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                onDismiss()
                                onDeleteClick(document)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VaultSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VaultCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = CrimsonAlert,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", color = CrimsonAlert, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
