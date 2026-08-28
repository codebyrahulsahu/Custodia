package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// CUSTODIA COLOR SYSTEM (LIGHT & DARK THEMES)
// ==========================================

// Primary Blue Palette
val RoyalBluePrimary = Color(0xFF1D4ED8)
val RoyalBlueLight = Color(0xFF3B82F6)
val RoyalBlueDark = Color(0xFF1E3A8A)
val RoyalBlueGlow = Color(0x331D4ED8)

// Secondary Cyan & Teal
val ElectricCyan = Color(0xFF0284C7)
val ElectricCyanLight = Color(0xFF38BDF8)
val TrustTeal = Color(0xFF0D9488)
val TrustTealLight = Color(0xFF14B8A6)
val TrustTealGlow = Color(0x260D9488)

// Status & Verification Accents
val VerifiedGreen = Color(0xFF059669)
val VerifiedGreenLight = Color(0xFF10B981)
val VerifiedGreenGlow = Color(0x20059669)

val AmberGold = Color(0xFFD97706)
val AmberGoldLight = Color(0xFFF59E0B)
val AmberGoldGlow = Color(0x20D97706)

val CrimsonAlert = Color(0xFFDC2626)
val CrimsonAlertLight = Color(0xFFEF4444)
val CrimsonAlertGlow = Color(0x20DC2626)

// ------------------------------------------
// LIGHT THEME TOKENS (Clean White & Slate)
// ------------------------------------------
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF1F5F9)
val LightSurfaceSecondary = Color(0xFFE2E8F0)
val LightCardBorder = Color(0xFFE2E8F0)
val LightCardBorderStrong = Color(0xFFCBD5E1)
val LightBlueSoftPill = Color(0xFFDBEAFE)
val LightBlueTint = Color(0xFFEFF6FF)

val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF64748B)
val LightTextDisabled = Color(0xFF94A3B8)

// ------------------------------------------
// DARK THEME TOKENS (Deep Obsidian & Slate)
// ------------------------------------------
val DarkBackground = Color(0xFF0B111A)
val DarkSurface = Color(0xFF131D2A)
val DarkSurfaceElevated = Color(0xFF1C2A3A)
val DarkSurfaceSecondary = Color(0xFF24364A)
val DarkCardBorder = Color(0xFF233649)
val DarkCardBorderStrong = Color(0xFF334E68)
val DarkBlueSoftPill = Color(0xFF1E3A5F)
val DarkBlueTint = Color(0xFF132337)

val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)
val DarkTextDisabled = Color(0xFF475569)

// Backwards-compatible aliases that are dynamically resolved or fallback cleanly
val BackgroundWhite = LightBackground
val VaultSurface = LightSurface
val VaultSurfaceElevated = LightSurfaceElevated
val VaultCardBorder = LightCardBorder
val VaultCardBorderLight = LightCardBorderStrong
val BlueSoftPill = LightBlueSoftPill
val BlueTintBackground = LightBlueTint
val TextPrimary = LightTextPrimary
val TextSecondary = LightTextSecondary
val TextMuted = LightTextMuted
val TextDisabled = LightTextDisabled
val VaultNavy = Color(0xFF0F172A)
val VaultNavyDark = Color(0xFF1E293B)
