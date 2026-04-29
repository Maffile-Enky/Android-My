package com.example.jjjjjppppp.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.jjjjjppppp.R
import java.util.Locale

object ThemeManager {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_LANGUAGE = "language"

    const val MODE_LIGHT = 0
    const val MODE_DARK = 1
    const val MODE_SYSTEM = 2

    const val LANG_ZH = "zh"
    const val LANG_EN = "en"

    // ==================== Theme ====================

    fun applyTheme(context: Context, mode: Int) {
        val nightMode = when (mode) {
            MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            MODE_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_THEME_MODE, mode)
            .apply()
    }

    fun getCurrentTheme(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_THEME_MODE, MODE_SYSTEM)
    }

    fun getThemeDisplayName(context: Context): String {
        return when (getCurrentTheme(context)) {
            MODE_LIGHT -> context.getString(R.string.theme_light)
            MODE_DARK -> context.getString(R.string.theme_dark)
            MODE_SYSTEM -> context.getString(R.string.theme_system)
            else -> context.getString(R.string.theme_system)
        }
    }

    // ==================== Language ====================

    fun applyLanguage(context: Context, langCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, langCode)
            .apply()
    }

    fun getCurrentLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, LANG_ZH) ?: LANG_ZH
    }

    fun getLanguageDisplayName(context: Context): String {
        return when (getCurrentLanguage(context)) {
            LANG_ZH -> context.getString(R.string.lang_chinese)
            LANG_EN -> "English"
            else -> context.getString(R.string.lang_chinese)
        }
    }

    fun wrapContext(context: Context): Context {
        val lang = getCurrentLanguage(context)
        val locale = when (lang) {
            LANG_EN -> Locale.ENGLISH
            else -> Locale.SIMPLIFIED_CHINESE
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
