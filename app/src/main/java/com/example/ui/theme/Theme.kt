package com.example.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CustodiaThemeMode(val title: String) {
    SYSTEM("System Default"),
    LIGHT("Light Theme"),
    DARK("Dark Theme")
}

object ThemePreferenceManager {
    private const val PREFS_NAME = "custodia_theme_prefs"
    private const val KEY_THEME = "selected_theme_mode"

    private val _currentThemeMode = MutableStateFlow(CustodiaThemeMode.LIGHT)
    val currentThemeMode: StateFlow<CustodiaThemeMode> = _currentThemeMode.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_THEME, CustodiaThemeMode.LIGHT.name) ?: CustodiaThemeMode.LIGHT.name
        val mode = try {
            CustodiaThemeMode.valueOf(savedName)
        } catch (_: Exception) {
            CustodiaThemeMode.LIGHT
        }
        _currentThemeMode.value = mode
    }

    fun setThemeMode(context: Context, mode: CustodiaThemeMode) {
        _currentThemeMode.value = mode
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }
}

@Immutable
data class CustodiaExtendedColors(
    val surfaceContainer: Color,
    val surfaceElevated: Color,
    val surfaceSecondary: Color,
    val cardBorder: Color,
    val cardBorderStrong: Color,
    val softBadgeBackground: Color,
    val softBadgeText: Color,
    val headerBackground: Color,
    val headerBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val isDark: Boolean
)

val LocalCustodiaColors = staticCompositionLocalOf {
    CustodiaExtendedColors(
        surfaceContainer = LightSurface,
        surfaceElevated = LightSurfaceElevated,
        surfaceSecondary = LightSurfaceSecondary,
        cardBorder = LightCardBorder,
        cardBorderStrong = LightCardBorderStrong,
        softBadgeBackground = LightBlueSoftPill,
        softBadgeText = RoyalBlueDark,
        headerBackground = LightSurface,
        headerBorder = LightCardBorder,
        textPrimary = LightTextPrimary,
        textSecondary = LightTextSecondary,
        textMuted = LightTextMuted,
        textDisabled = LightTextDisabled,
        isDark = false
    )
}

val CustodiaLightColorScheme = lightColorScheme(
    primary = RoyalBluePrimary,
    onPrimary = Color.White,
    primaryContainer = LightBlueSoftPill,
    onPrimaryContainer = RoyalBlueDark,
    secondary = ElectricCyan,
    onSecondary = Color.White,
    secondaryContainer = LightBlueTint,
    onSecondaryContainer = RoyalBlueDark,
    tertiary = VerifiedGreen,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    error = CrimsonAlert,
    onError = Color.White,
    outline = LightCardBorder,
    outlineVariant = LightCardBorderStrong
)

val CustodiaDarkColorScheme = darkColorScheme(
    primary = ElectricCyanLight,
    onPrimary = Color(0xFF0B111A),
    primaryContainer = DarkBlueSoftPill,
    onPrimaryContainer = Color(0xFFE2E8F0),
    secondary = TrustTealLight,
    onSecondary = Color(0xFF0B111A),
    secondaryContainer = DarkBlueTint,
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = VerifiedGreenLight,
    onTertiary = Color(0xFF0B111A),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    error = CrimsonAlertLight,
    onError = Color.White,
    outline = DarkCardBorder,
    outlineVariant = DarkCardBorderStrong
)

@Composable
fun MyApplicationTheme(
    themeMode: CustodiaThemeMode = CustodiaThemeMode.LIGHT,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        CustodiaThemeMode.SYSTEM -> isSystemDark
        CustodiaThemeMode.LIGHT -> false
        CustodiaThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) CustodiaDarkColorScheme else CustodiaLightColorScheme

    val extendedColors = if (isDark) {
        CustodiaExtendedColors(
            surfaceContainer = DarkSurface,
            surfaceElevated = DarkSurfaceElevated,
            surfaceSecondary = DarkSurfaceSecondary,
            cardBorder = DarkCardBorder,
            cardBorderStrong = DarkCardBorderStrong,
            softBadgeBackground = DarkBlueSoftPill,
            softBadgeText = Color(0xFF93C5FD),
            headerBackground = DarkSurface,
            headerBorder = DarkCardBorder,
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            textMuted = DarkTextMuted,
            textDisabled = DarkTextDisabled,
            isDark = true
        )
    } else {
        CustodiaExtendedColors(
            surfaceContainer = LightSurface,
            surfaceElevated = LightSurfaceElevated,
            surfaceSecondary = LightSurfaceSecondary,
            cardBorder = LightCardBorder,
            cardBorderStrong = LightCardBorderStrong,
            softBadgeBackground = LightBlueSoftPill,
            softBadgeText = RoyalBlueDark,
            headerBackground = LightSurface,
            headerBorder = LightCardBorder,
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textMuted = LightTextMuted,
            textDisabled = LightTextDisabled,
            isDark = false
        )
    }

    CompositionLocalProvider(LocalCustodiaColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
