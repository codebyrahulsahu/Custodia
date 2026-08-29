package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DocumentItem
import com.example.data.FamilyMemberProfile
import com.example.data.MedicalEntry
import com.example.data.MemberSignature
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TrustTeal
import com.example.ui.theme.VerifiedGreen

@Composable
fun HomeScreen(
    familyMembers: List<FamilyMemberProfile>,
    documents: List<DocumentItem>,
    signatures: List<MemberSignature>,
    medicalEntries: List<MedicalEntry>,
    onSelectMember: (String) -> Unit,
    onAddMemberClick: () -> Unit,
    onEditMemberClick: (FamilyMemberProfile) -> Unit,
    onDeleteMemberClick: (String) -> Unit,
    onExportMemberPdf: (Context, FamilyMemberProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Vault Overview Statistics Banner
        item {
            VaultStatsOverviewBanner(
                memberCount = familyMembers.size,
                docCount = documents.size,
                sigCount = signatures.size,
                medCount = medicalEntries.size
            )
        }

        // Section Title & Add Member Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FAMILY MEMBERS (${familyMembers.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Tap to open dossier",
                    fontSize = 11.5.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                )
            }
        }

        // Family Members List
        if (familyMembers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                        .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f), modifier = Modifier.size(48.dp))
                        Text(
                            text = "No Family Members Added",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Add family members to securely manage their documents, signatures, and health records.",
                            fontSize = 12.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onAddMemberClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TrustTeal)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add First Member", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        } else {
            items(familyMembers, key = { it.id }) { member ->
                val memberDocs = documents.filter { it.memberId == member.id }
                val memberSig = signatures.find { it.memberId == member.id }
                val memberMeds = medicalEntries.filter { it.memberId == member.id }

                FamilyMemberCard(
                    member = member,
                    docCount = memberDocs.size,
                    hasSignature = memberSig != null,
                    medCount = memberMeds.size,
                    onClick = { onSelectMember(member.id) },
                    onEditClick = { onEditMemberClick(member) },
                    onDeleteClick = { onDeleteMemberClick(member.id) },
                    onExportPdf = { onExportMemberPdf(context, member) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VaultStatsOverviewBanner(
    memberCount: Int,
    docCount: Int,
    sigCount: Int,
    medCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(title = "Members", count = "$memberCount", color = TrustTeal)
            Divider(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp),
                color = androidx.compose.material3.MaterialTheme.colorScheme.outline
            )
            StatItem(title = "Documents", count = "$docCount", color = ElectricCyan)
            Divider(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp),
                color = androidx.compose.material3.MaterialTheme.colorScheme.outline
            )
            StatItem(title = "Signatures", count = "$sigCount", color = VerifiedGreen)
            Divider(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp),
                color = androidx.compose.material3.MaterialTheme.colorScheme.outline
            )
            StatItem(title = "Medical", count = "$medCount", color = Color(0xFFF59E0B))
        }
    }
}

@Composable
private fun StatItem(title: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = title,
            fontSize = 10.5.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FamilyMemberCard(
    member: FamilyMemberProfile,
    docCount: Int,
    hasSignature: Boolean,
    medCount: Int,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("card_member_${member.name.replace(" ", "_")}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Avatar, Info, Action Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(member.avatarColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.avatarInitials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = member.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Relationship Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TrustTeal.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = member.relationshipLabel,
                                    color = TrustTeal,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Blood Group Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Blood: ${member.bloodGroup}",
                                    color = Color(0xFFF87171),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Quick PDF download & Options Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onExportPdf,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = TrustTeal, modifier = Modifier.size(18.dp))
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Member", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Profile", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    menuExpanded = false
                                    onEditClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TrustTeal) }
                            )
                            DropdownMenuItem(
                                text = { Text("Download Full PDF", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    menuExpanded = false
                                    onExportPdf()
                                },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = ElectricCyan) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Member", color = Color(0xFFEF4444)) },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) }
                            )
                        }
                    }

                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Open Dossier",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Member Info Row (DOB & Phone)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DOB: ${member.dob}",
                    fontSize = 11.5.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (member.phone.isNotBlank()) {
                    Text(
                        text = member.phone,
                        fontSize = 11.5.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider(color = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))

            // Stat Badges: Documents, Signature, Medical
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Documents count chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$docCount Docs",
                        fontSize = 11.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Signature status chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Draw,
                        contentDescription = null,
                        tint = if (hasSignature) VerifiedGreen else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (hasSignature) "Signed" else "No Signature",
                        fontSize = 11.sp,
                        color = if (hasSignature) VerifiedGreen else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Medical records chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$medCount Medical",
                        fontSize = 11.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
