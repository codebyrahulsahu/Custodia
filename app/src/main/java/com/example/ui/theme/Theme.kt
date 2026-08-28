package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CustodiaColorScheme = darkColorScheme(
    primary = TrustTealLight,
    onPrimary = Color.Black,
    primaryContainer = TrustTeal,
    onPrimaryContainer = Color.White,
    secondary = AmberGold,
    onSecondary = Color.Black,
    secondaryContainer = AmberGoldLight,
    onSecondaryContainer = Color.Black,
    tertiary = VerifiedGreen,
    onTertiary = Color.Black,
    background = VaultNavy,
    onBackground = TextPrimary,
    surface = VaultSurface,
    onSurface = TextPrimary,
    surfaceVariant = VaultSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = CrimsonAlert,
    onError = Color.White,
    outline = VaultCardBorder,
    outlineVariant = VaultCardBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Forced custom vault theme for consistent high-security look
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CustodiaColorScheme,
        typography = Typography,
        content = content
    )
}
