package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.DriveAccountInfo
import com.example.data.DriveBackupInfo
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.TrustTeal
import com.example.ui.theme.VerifiedGreen

@Composable
fun GoogleDriveBackupModal(
    driveAccount: DriveAccountInfo,
    backups: List<DriveBackupInfo>,
    isBackingUp: Boolean,
    onDismiss: () -> Unit,
    onBackupNow: () -> Unit,
    onRestoreBackup: (DriveBackupInfo) -> Unit,
    onDeleteBackup: ((String) -> Unit)? = null
) {
    var backupToRestore by remember { mutableStateOf<DriveBackupInfo?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AmberGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Google Drive AppData Sync",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Dedicated AppData Vault",
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f))
                    }
                }

                // Account Connection & AppData Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                        .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(TrustTeal.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("G", fontWeight = FontWeight.Bold, color = TrustTeal, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = driveAccount.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = driveAccount.email,
                                        fontSize = 11.sp,
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("OAuth Active", fontSize = 11.sp, color = VerifiedGreen, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = androidx.compose.material3.MaterialTheme.colorScheme.outline)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Space: ${driveAccount.appFolder}", fontSize = 11.5.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Text(
                                text = "Last: ${driveAccount.lastBackupTime ?: "Never"}",
                                fontSize = 11.sp,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                            )
                        }
                    }
                }

                // Backup Now Action Button
                Button(
                    onClick = onBackupNow,
                    enabled = !isBackingUp,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_perform_drive_backup")
                ) {
                    if (isBackingUp) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Syncing with Google Drive...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Backup Local Vault to Google Drive", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Backups in Google Drive AppData Folder
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "BACKUP SNAPSHOTS IN APPDATA FOLDER (${backups.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )

                    if (backups.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No backups found in Google Drive AppData folder.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f), fontSize = 12.sp)
                        }
                    } else {
                        backups.forEach { backup ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                                    .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = backup.formattedDate,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${backup.memberCount} Members • ${backup.documentCount} Docs • ${backup.medicalCount} Med Records • ${backup.signatureCount} Sigs",
                                            fontSize = 11.sp,
                                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Size: ${backup.fileSize} • ID: ${backup.driveFileId.take(16)}...",
                                            fontSize = 10.5.sp,
                                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (onDeleteBackup != null) {
                                            IconButton(
                                                onClick = { onDeleteBackup(backup.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f), modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }

                                        Button(
                                            onClick = { backupToRestore = backup },
                                            enabled = !isBackingUp,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.Restore, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Restore", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Security & Privacy Guarantee Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hidden from general Drive view • Stored in AppData folder",
                        fontSize = 11.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Restore Confirmation Dialog
    if (backupToRestore != null) {
        val b = backupToRestore!!
        AlertDialog(
            onDismissRequest = { backupToRestore = null },
            title = {
                Text("Restore from Google Drive?", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "This will replace local database records with the snapshot from:",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Text(
                        b.formattedDate,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold,
                        fontSize = 13.5.sp
                    )
                    Text(
                        "Containing ${b.memberCount} members, ${b.documentCount} documents, ${b.medicalCount} medical entries, and ${b.signatureCount} signatures.",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = backupToRestore
                        backupToRestore = null
                        if (target != null) {
                            onRestoreBackup(target)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrustTeal)
                ) {
                    Text("Confirm Restore", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { backupToRestore = null }) {
                    Text("Cancel", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    }
}
