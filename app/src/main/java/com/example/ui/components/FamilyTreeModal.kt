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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FamilyMemberProfile
import com.example.data.RelationshipType
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrustTeal
import com.example.ui.theme.VaultCardBorder
import com.example.ui.theme.VaultNavyDark
import com.example.ui.theme.VaultSurface

@Composable
fun FamilyTreeModal(
    familyMembers: List<FamilyMemberProfile>,
    onDismiss: () -> Unit,
    onSelectMember: (FamilyMemberProfile) -> Unit
) {
    // Group members by generation
    val gen1Parents = familyMembers.filter { it.generation == 1 || it.relationship == RelationshipType.FATHER || it.relationship == RelationshipType.MOTHER || it.relationship == RelationshipType.IN_LAWS }
    val gen2Couples = familyMembers.filter { (it.generation == 2 || it.relationship == RelationshipType.HEAD || it.relationship == RelationshipType.SPOUSE) && !gen1Parents.contains(it) }
    val gen3Children = familyMembers.filter { it.generation == 3 || it.relationship == RelationshipType.SON || it.relationship == RelationshipType.DAUGHTER }
    val others = familyMembers.filter { !gen1Parents.contains(it) && !gen2Couples.contains(it) && !gen3Children.contains(it) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, VaultCardBorder, RoundedCornerShape(16.dp)),
            color = VaultNavyDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Modal Header
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
                                .background(ElectricCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountTree, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Family Tree & Lineage",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "${familyMembers.size} Connected Members",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                // Generational Tree Diagram Canvas
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VaultSurface)
                        .border(1.dp, VaultCardBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // GENERATION 1: PARENTS / SENIORS
                    if (gen1Parents.isNotEmpty()) {
                        Text(
                            text = "SENIOR GENERATION • PARENTS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 1.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            gen1Parents.forEach { member ->
                                FamilyTreeNode(member = member, onClick = {
                                    onSelectMember(member)
                                    onDismiss()
                                })
                            }
                        }

                        // Connecting Arrow
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = VaultCardBorder, modifier = Modifier.size(18.dp))
                    }

                    // GENERATION 2: HEAD & SPOUSE
                    if (gen2Couples.isNotEmpty()) {
                        Text(
                            text = "PRIMARY GENERATION • HEAD & SPOUSE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrustTeal,
                            letterSpacing = 1.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            gen2Couples.forEachIndexed { index, member ->
                                FamilyTreeNode(member = member, onClick = {
                                    onSelectMember(member)
                                    onDismiss()
                                })
                                if (index == 0 && gen2Couples.size > 1) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Spouse Link", tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        if (gen3Children.isNotEmpty()) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = VaultCardBorder, modifier = Modifier.size(18.dp))
                        }
                    }

                    // GENERATION 3: CHILDREN
                    if (gen3Children.isNotEmpty()) {
                        Text(
                            text = "NEXT GENERATION • CHILDREN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B),
                            letterSpacing = 1.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            gen3Children.forEach { member ->
                                FamilyTreeNode(member = member, onClick = {
                                    onSelectMember(member)
                                    onDismiss()
                                })
                            }
                        }
                    }

                    if (others.isNotEmpty()) {
                        Divider(color = VaultCardBorder, modifier = Modifier.padding(vertical = 4.dp))
                        Text("OTHER FAMILY MEMBERS", fontSize = 10.sp, color = TextMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            others.forEach { member ->
                                FamilyTreeNode(member = member, onClick = {
                                    onSelectMember(member)
                                    onDismiss()
                                })
                            }
                        }
                    }
                }

                Text(
                    text = "Tip: Tap on any family member card to view their complete dossier, documents, signature, and medical records.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FamilyTreeNode(
    member: FamilyMemberProfile,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(VaultNavyDark)
            .border(1.dp, VaultCardBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp)
            .width(100.dp)
            .testTag("family_tree_node_${member.name.replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(member.avatarColorHex)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.avatarInitials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = member.name,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            color = TextPrimary,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Text(
            text = member.relationship.label,
            fontSize = 9.5.sp,
            color = TextSecondary,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Blood: ${member.bloodGroup}",
            fontSize = 8.5.sp,
            color = TrustTeal,
            fontWeight = FontWeight.Medium
        )
    }
}
