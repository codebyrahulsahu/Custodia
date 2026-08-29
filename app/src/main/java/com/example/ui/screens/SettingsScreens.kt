package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.appStr
import com.example.ui.theme.CustodiaThemeMode
import com.example.ui.theme.TrustTeal

@Composable
fun SettingsScreen(
    themeMode: CustodiaThemeMode,
    language: AppLanguage,
    onThemeSelected: (CustodiaThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onBackupRestoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(appStr("settings_title"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("Personalize your secure family vault.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        item {
            SettingsCard(Icons.Default.DarkMode, appStr("settings_appearance"), "Choose a comfortable, high-contrast appearance") {
                CustodiaThemeMode.entries.forEach { mode ->
                    ChoiceRow(mode.title, themeMode == mode) { onThemeSelected(mode) }
                }
            }
        }
        item {
            SettingsCard(Icons.Default.Backup, appStr("settings_backup"), "Keep a recoverable copy of your vault") {
                Button(
                    onClick = onBackupRestoreClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CloudSync, null)
                    Text("  Open Backup & Restore", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text("Create, browse, restore, or remove encrypted Google Drive backups.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SettingsCard(Icons.Default.Language, appStr("settings_language"), "Select the language used by navigation and key actions") {
                AppLanguage.entries.forEach { item ->
                    ChoiceRow(item.nativeName, language == item) { onLanguageSelected(item) }
                }
            }
        }
        item {
            SettingsCard(Icons.Default.Security, "Privacy & security", "Your files remain in the private app vault") {
                InfoRow(Icons.Default.Fingerprint, "Private on-device file storage")
                InfoRow(Icons.Default.Shield, "Files are shared only when you explicitly choose Share")
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 1.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Column(Modifier.padding(start = 10.dp)) {
                    Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            content()
        }
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TrustTeal, modifier = Modifier.size(18.dp))
        Text(text, Modifier.padding(start = 9.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(88.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Shield, "Custodia", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(appStr("about_title"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text("Your family's important records, together and protected.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(Icons.Default.Info, "Custodia 1.0")
                InfoRow(Icons.Default.Security, "Offline-first private family vault")
                InfoRow(Icons.Default.Backup, "Backup and restore support")
                Text("Custodia helps organize identity documents, signature specimens, and medical records for every member of your family.", color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Built with privacy in mind", color = TrustTeal, fontWeight = FontWeight.SemiBold)
    }
}
