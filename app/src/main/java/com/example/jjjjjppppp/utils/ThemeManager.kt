package com.example.jjjjjppppp.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.jjjjjppppp.R
import java.util.Locale

object ThemeManager {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_COLOR_THEME = "color_theme"
    private const val KEY_LANGUAGE = "language"

    // 日夜间模式
    const val MODE_LIGHT = 0
    const val MODE_DARK = 1
    const val MODE_SYSTEM = 2

    // 主题色系
    const val THEME_BLUE = 0
    const val THEME_GREEN = 1
    const val THEME_ORANGE = 2
    const val THEME_PURPLE = 3
    const val THEME_PINK = 4
    const val THEME_TEAL = 5

    const val LANG_ZH = "zh"
    const val LANG_EN = "en"

    private val THEME_STYLE_IDS = intArrayOf(
        R.style.Theme_Jjjjjppppp_Blue,   // 天空蓝
        R.style.Theme_Jjjjjppppp,        // 清新绿
        R.style.Theme_Jjjjjppppp_Orange, // 活力橙
        R.style.Theme_Jjjjjppppp_Purple, // 优雅紫
        R.style.Theme_Jjjjjppppp_Pink,   // 浪漫粉
        R.style.Theme_Jjjjjppppp_Teal    // 深邃青
    )

    private val THEME_NAME_KEYS = intArrayOf(
        R.string.theme_blue,
        R.string.theme_green,
        R.string.theme_orange,
        R.string.theme_purple,
        R.string.theme_pink,
        R.string.theme_teal
    )

    private val THEME_PRIMARY_COLORS = intArrayOf(
        0xFF2196F3.toInt(),
        0xFF4CAF50.toInt(),
        0xFF9800.toInt(),
        0xFF9C27B0.toInt(),
        0xFFE91E63.toInt(),
        0xFF009688.toInt()
    )

    // ==================== Theme Mode (Light/Dark/System) ====================

    fun applyThemeMode(context: Context, mode: Int) {
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

    fun getCurrentThemeMode(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_THEME_MODE, MODE_SYSTEM)
    }

    fun getThemeModeDisplayName(context: Context): String {
        return when (getCurrentThemeMode(context)) {
            MODE_LIGHT -> context.getString(R.string.theme_light)
            MODE_DARK -> context.getString(R.string.theme_dark)
            MODE_SYSTEM -> context.getString(R.string.theme_system)
            else -> context.getString(R.string.theme_system)
        }
    }

    // ==================== Color Theme ====================

    fun applyColorTheme(context: Context, theme: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_COLOR_THEME, theme)
            .apply()
    }

    fun getCurrentColorTheme(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_COLOR_THEME, THEME_BLUE)
    }

    fun getColorThemeStyle(context: Context): Int {
        val index = getCurrentColorTheme(context)
        return THEME_STYLE_IDS.getOrElse(index) { R.style.Theme_Jjjjjppppp_Blue }
    }

    fun getColorThemeName(context: Context): String {
        val index = getCurrentColorTheme(context)
        val key = THEME_NAME_KEYS.getOrElse(index) { R.string.theme_blue }
        return context.getString(key)
    }

    fun getColorThemePrimaryColor(context: Context): Int {
        val index = getCurrentColorTheme(context)
        return THEME_PRIMARY_COLORS.getOrElse(index) { 0xFF2196F3.toInt() }
    }

    fun getThemePrimaryColorFor(index: Int): Int {
        return THEME_PRIMARY_COLORS.getOrElse(index) { 0xFF2196F3.toInt() }
    }

    fun getThemeNameKeyFor(index: Int): Int {
        return THEME_NAME_KEYS.getOrElse(index) { R.string.theme_blue }
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
