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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DriveAccountInfo
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TrustTeal
import com.example.ui.theme.VerifiedGreen

@Composable
fun AppHeader(
    driveAccount: DriveAccountInfo,
    onFamilyTreeClick: () -> Unit,
    onDriveBackupClick: () -> Unit,
    onAddMemberClick: () -> Unit,
    showAddMemberButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    SurfaceHeader(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App Logo & Brand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("app_branding")
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TrustTeal.copy(alpha = 0.18f))
                            .border(1.dp, TrustTeal.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Custodia Logo",
                            tint = TrustTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Custodia",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Family Vault & Records",
                            style = MaterialTheme.typography.labelSmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Actions: Family Tree & Google Drive & Add Member
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Family Tree Button
                    IconButton(
                        onClick = onFamilyTreeClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, CircleShape)
                            .testTag("btn_family_tree")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = "Family Tree",
                            tint = ElectricCyan,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Google Drive Backup Button
                    IconButton(
                        onClick = onDriveBackupClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, CircleShape)
                            .testTag("btn_google_drive_backup")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Google Drive Backup",
                            tint = AmberGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (showAddMemberButton) {
                        Button(
                            onClick = onAddMemberClick,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("btn_header_add_member")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Member",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Google Drive Status Banner / Indicator Chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .clickable { onDriveBackupClick() }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Drive Sync",
                        tint = VerifiedGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Google Drive Backup: ${driveAccount.lastBackupTime ?: "Not backed up yet"}",
                        fontSize = 11.5.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "Manage →",
                    fontSize = 11.5.sp,
                    color = TrustTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SurfaceHeader(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
    ) {
        content()
    }
}
