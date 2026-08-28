package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    SPANISH("es", "Spanish", "Español"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી"),
    MARATHI("mr", "Marathi", "मराठी"),
    FRENCH("fr", "French", "Français")
}

object LocalizationManager {
    private const val PREFS_NAME = "custodia_lang_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCode = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
        val lang = AppLanguage.values().find { it.code == savedCode } ?: AppLanguage.ENGLISH
        _currentLanguage.value = lang
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        _currentLanguage.value = language
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    fun getString(key: String, vararg args: Any): String {
        val lang = _currentLanguage.value
        val raw = translations[lang]?.get(key)
            ?: translations[AppLanguage.ENGLISH]?.get(key)
            ?: key
        return if (args.isNotEmpty()) {
            try {
                String.format(raw, *args)
            } catch (_: Exception) {
                raw
            }
        } else {
            raw
        }
    }

    private val translations: Map<AppLanguage, Map<String, String>> = mapOf(
        AppLanguage.ENGLISH to mapOf(
            "app_name" to "Custodia",
            "app_tagline" to "Family Vault & Health Locker",
            "nav_vault" to "Vault",
            "nav_settings" to "Settings",
            "nav_about" to "About",
            "tab_documents" to "Documents",
            "tab_signature" to "Signature",
            "tab_medical" to "Medical History",
            "add_member" to "Add Member",
            "add_document" to "Add Document",
            "add_record" to "Add Record",
            "draw_signature" to "Draw / Upload Signature",
            "update_signature" to "Update Signature",
            "download_signature" to "Download Signature",
            "share_signature" to "Share Signature",
            "view_document" to "View Document",
            "download_document" to "Download",
            "share_document" to "Share Real File",
            "export_pdf" to "Export PDF",
            "settings_title" to "Settings & Preferences",
            "settings_appearance" to "Appearance",
            "settings_theme" to "Theme Mode",
            "settings_theme_light" to "Light Theme",
            "settings_theme_dark" to "Dark Theme",
            "settings_theme_system" to "System Default",
            "settings_backup" to "Backup & Restore",
            "settings_google_drive" to "Google Drive Cloud Backup",
            "settings_local_backup" to "Local Vault Export / Import",
            "settings_language" to "Language Preference",
            "settings_security" to "Security & Encryption",
            "about_title" to "About Custodia",
            "about_version" to "Version 2.4.0 (Build 2026.08)",
            "about_security_desc" to "100% On-Device Offline-First Security with AES-256 Vault Architecture.",
            "family_tree" to "Family Tree",
            "members" to "Members",
            "documents" to "Documents",
            "signatures" to "Signatures",
            "medical" to "Medical",
            "all_members" to "All Family Members",
            "no_members" to "No Family Members Added",
            "no_docs" to "No Documents Added",
            "no_records" to "No Medical Records",
            "blood_group" to "Blood Group",
            "allergies" to "Allergies",
            "chronic_conditions" to "Chronic Conditions",
            "medications" to "Medications",
            "past_illness" to "Past Illnesses / Surgeries",
            "notes" to "Clinical Notes",
            "take_photo" to "Take Photo",
            "file_manager" to "File Manager"
        ),
        AppLanguage.HINDI to mapOf(
            "app_name" to "कस्टोडिया",
            "app_tagline" to "पारिवारिक वॉल्ट और स्वास्थ्य लॉकर",
            "nav_vault" to "वॉल्ट",
            "nav_settings" to "सेटिंग्स",
            "nav_about" to "के बारे में",
            "tab_documents" to "दस्तावेज़",
            "tab_signature" to "हस्ताक्षर",
            "tab_medical" to "स्वास्थ्य इतिहास",
            "add_member" to "सदस्य जोड़ें",
            "add_document" to "दस्तावेज़ जोड़ें",
            "add_record" to "रिकॉर्ड जोड़ें",
            "draw_signature" to "हस्ताक्षर बनाएं / अपलोड करें",
            "update_signature" to "हस्ताक्षर अपडेट करें",
            "download_signature" to "हस्ताक्षर डाउनलोड करें",
            "share_signature" to "हस्ताक्षर शेयर करें",
            "view_document" to "दस्तावेज़ देखें",
            "download_document" to "डाउनलोड",
            "share_document" to "दस्तावेज़ शेयर करें",
            "export_pdf" to "PDF एक्सपोर्ट",
            "settings_title" to "सेटिंग्स और प्राथमिकताएं",
            "settings_appearance" to "दिखावट",
            "settings_theme" to "थीम मोड",
            "settings_theme_light" to "लाइट थीम",
            "settings_theme_dark" to "डार्क थीम",
            "settings_theme_system" to "सिस्टम डिफ़ॉल्ट",
            "settings_backup" to "बैकअप और रीस्टोर",
            "settings_google_drive" to "गूगल ड्राइव क्लाउड बैकअप",
            "settings_local_backup" to "लोकल वॉल्ट एक्सपोर्ट / इम्पोर्ट",
            "settings_language" to "भाषा प्राथमिकता",
            "settings_security" to "सुरक्षा और एन्क्रिप्शन",
            "about_title" to "कस्टोडिया के बारे में",
            "about_version" to "संस्करण 2.4.0 (बिल्ड 2026.08)",
            "about_security_desc" to "AES-256 वॉल्ट आर्किटेक्चर के साथ 100% सुरक्षित ऑफलाइन स्टोरेज।",
            "family_tree" to "परिवार वृक्ष",
            "members" to "सदस्य",
            "documents" to "दस्तावेज़",
            "signatures" to "हस्ताक्षर",
            "medical" to "स्वास्थ्य",
            "all_members" to "सभी परिवार के सदस्य",
            "no_members" to "कोई सदस्य नहीं जोड़ा गया",
            "no_docs" to "कोई दस्तावेज़ नहीं",
            "no_records" to "कोई मेडिकल रिकॉर्ड नहीं",
            "blood_group" to "ब्लड ग्रुप",
            "allergies" to "एलर्जी",
            "chronic_conditions" to "पुरानी बीमारियाँ",
            "medications" to "दवाइयाँ",
            "past_illness" to "पिछली बीमारियाँ / सर्जरी",
            "notes" to "डॉक्टर नोट्स",
            "take_photo" to "फोटो लें",
            "file_manager" to "फाइल मैनेजर"
        ),
        AppLanguage.SPANISH to mapOf(
            "app_name" to "Custodia",
            "app_tagline" to "Bóveda Familiar y Salud",
            "nav_vault" to "Bóveda",
            "nav_settings" to "Ajustes",
            "nav_about" to "Acerca de",
            "tab_documents" to "Documentos",
            "tab_signature" to "Firma",
            "tab_medical" to "Historial Médico",
            "add_member" to "Añadir Miembro",
            "add_document" to "Añadir Documento",
            "add_record" to "Añadir Registro",
            "draw_signature" to "Firmar / Subir Firma",
            "update_signature" to "Actualizar Firma",
            "download_signature" to "Descargar Firma",
            "share_signature" to "Compartir Firma",
            "view_document" to "Ver Documento",
            "download_document" to "Descargar",
            "share_document" to "Compartir Archivo Real",
            "export_pdf" to "Exportar PDF",
            "settings_title" to "Ajustes y Preferencias",
            "settings_appearance" to "Apariencia",
            "settings_theme" to "Modo de Tema",
            "settings_theme_light" to "Tema Claro",
            "settings_theme_dark" to "Tema Oscuro",
            "settings_theme_system" to "Predeterminado",
            "settings_backup" to "Copia de Seguridad y Restauración",
            "settings_google_drive" to "Copia en Google Drive",
            "settings_local_backup" to "Exportar / Importar Local",
            "settings_language" to "Preferencia de Idioma",
            "settings_security" to "Seguridad y Encriptación",
            "about_title" to "Acerca de Custodia",
            "about_version" to "Versión 2.4.0",
            "about_security_desc" to "Almacenamiento seguro 100% local con cifrado AES-256.",
            "family_tree" to "Árbol Genealógico",
            "members" to "Miembros",
            "documents" to "Documentos",
            "signatures" to "Firmas",
            "medical" to "Médico",
            "all_members" to "Todos los Miembros",
            "take_photo" to "Tomar Foto",
            "file_manager" to "Archivos"
        ),
        AppLanguage.GUJARATI to mapOf(
            "app_name" to "કસ્ટોડિયા",
            "app_tagline" to "ફેમિલી વૉલ્ટ અને હેલ્થ લોકર",
            "nav_vault" to "વૉલ્ટ",
            "nav_settings" to "સેટિંગ્સ",
            "nav_about" to "વિશે",
            "tab_documents" to "દસ્તાવેજો",
            "tab_signature" to "સહી",
            "tab_medical" to "મેડિકલ હિસ્ટ્રી",
            "add_member" to "સભ્ય ઉમેરો",
            "add_document" to "દસ્તાવેજ ઉમેરો",
            "add_record" to "મેડિકલ રેકોર્ડ ઉમેરો",
            "draw_signature" to "સહી કરો / અપલોડ કરો",
            "download_signature" to "સહી ડાઉનલોડ કરો",
            "share_signature" to "સહી શેર કરો",
            "view_document" to "દસ્તાવેજ જુઓ",
            "download_document" to "ડાઉનલોડ",
            "share_document" to "શેર કરો",
            "export_pdf" to "PDF એક્સપોર્ટ",
            "settings_title" to "સેટિંગ્સ",
            "settings_theme" to "થીમ મોડ",
            "settings_backup" to "બેકઅપ અને રિસ્ટોર",
            "settings_language" to "ભાષા પસંદગી",
            "about_title" to "કસ્ટોડિયા વિશે",
            "family_tree" to "ફેમિલી ટ્રી",
            "members" to "સભ્યો",
            "documents" to "દસ્તાવેજો",
            "signatures" to "સહીઓ",
            "medical" to "મેડિકલ",
            "take_photo" to "ફોટો લો",
            "file_manager" to "ફાઇલ મેનેજર"
        ),
        AppLanguage.MARATHI to mapOf(
            "app_name" to "कस्टोडिया",
            "app_tagline" to "कौटुंबिक वॉल्ट आणि हेल्थ लॉकर",
            "nav_vault" to "वॉल्ट",
            "nav_settings" to "सेटिंग्ज",
            "nav_about" to "माहिती",
            "tab_documents" to "कागदपत्रे",
            "tab_signature" to "स्वाक्षरी",
            "tab_medical" to "वैद्यकीय इतिहास",
            "add_member" to "सदस्य जोडा",
            "add_document" to "कागदपत्र जोडा",
            "add_record" to "रेकॉर्ड जोडा",
            "draw_signature" to "स्वाक्षरी करा",
            "download_signature" to "स्वाक्षरी डाउनलोड करा",
            "share_signature" to "स्वाक्षरी शेअर करा",
            "view_document" to "कागदपत्र पहा",
            "download_document" to "डाउनलोड",
            "share_document" to "शेअर करा",
            "export_pdf" to "PDF एक्सपोर्ट",
            "settings_title" to "सेटिंग्ज",
            "settings_theme" to "थीम मोड",
            "settings_backup" to "बॅकअप आणि रिस्टोअर",
            "settings_language" to "भाषा निवड",
            "about_title" to "कस्टोडिया बद्दल",
            "family_tree" to "कुटुंब वृक्ष",
            "members" to "सदस्य",
            "documents" to "कागदपत्रे",
            "signatures" to "स्वाक्षऱ्या",
            "medical" to "वैद्यकीय",
            "take_photo" to "फोटो घ्या",
            "file_manager" to "फाइल मॅनेजर"
        ),
        AppLanguage.FRENCH to mapOf(
            "app_name" to "Custodia",
            "app_tagline" to "Coffre Familial et Santé",
            "nav_vault" to "Coffre",
            "nav_settings" to "Paramètres",
            "nav_about" to "À propos",
            "tab_documents" to "Documents",
            "tab_signature" to "Signature",
            "tab_medical" to "Santé",
            "add_member" to "Ajouter Membre",
            "add_document" to "Ajouter Document",
            "add_record" to "Ajouter Dossier",
            "draw_signature" to "Signer / Téléverser",
            "download_signature" to "Télécharger Signature",
            "share_signature" to "Partager Signature",
            "view_document" to "Voir Document",
            "download_document" to "Télécharger",
            "share_document" to "Partager le fichier",
            "export_pdf" to "Exporter PDF",
            "settings_title" to "Paramètres",
            "settings_theme" to "Mode Thème",
            "settings_backup" to "Sauvegarde et Restauration",
            "settings_language" to "Langue",
            "about_title" to "À propos de Custodia",
            "family_tree" to "Arbre Familial",
            "members" to "Membres",
            "documents" to "Documents",
            "signatures" to "Signatures",
            "medical" to "Médical",
            "take_photo" to "Prendre Photo",
            "file_manager" to "Fichiers"
        )
    )
}

fun appStr(key: String, vararg args: Any): String {
    return LocalizationManager.getString(key, *args)
}
