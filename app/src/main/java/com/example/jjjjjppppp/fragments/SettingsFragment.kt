package com.example.jjjjjppppp.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.jjjjjppppp.R
import com.example.jjjjjppppp.utils.ThemeManager
import com.example.jjjjjppppp.utils.UpdateChecker

class SettingsFragment : Fragment() {

    private lateinit var tvThemeValue: TextView
    private lateinit var tvColorThemeValue: TextView
    private lateinit var vColorPreview: View
    private lateinit var tvLanguageValue: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbarSettings)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        tvThemeValue = view.findViewById(R.id.tvThemeValue)
        tvColorThemeValue = view.findViewById(R.id.tvColorThemeValue)
        vColorPreview = view.findViewById(R.id.vColorPreview)
        tvLanguageValue = view.findViewById(R.id.tvLanguageValue)

        updateAllDisplays()

        view.findViewById<View>(R.id.llColorTheme).setOnClickListener { showColorThemeDialog() }
        view.findViewById<View>(R.id.llTheme).setOnClickListener { showThemeModeDialog() }
        view.findViewById<View>(R.id.llLanguage).setOnClickListener { showLanguageDialog() }
        view.findViewById<View>(R.id.llClearCache).setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.llCheckUpdate).setOnClickListener {
            UpdateChecker.checkForUpdate(requireContext())
        }
        view.findViewById<View>(R.id.llAbout).setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.about_info), Toast.LENGTH_LONG).show()
        }
        view.findViewById<View>(R.id.llFeedback).setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.thanks_feedback), Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.llPrivacy).setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.privacy_policy_text), Toast.LENGTH_LONG).show()
        }
    }

    private fun showColorThemeDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(createColorThemePickerView(dialog))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    private fun createColorThemePickerView(dialog: Dialog): View {
        val context = requireContext()
        val currentTheme = ThemeManager.getCurrentColorTheme(context)
        val themeNames = arrayOf(
            R.string.theme_green, R.string.theme_blue, R.string.theme_orange,
            R.string.theme_purple, R.string.theme_pink, R.string.theme_teal
        )

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 32)
        }

        val title = TextView(context).apply {
            text = getString(R.string.select_color_theme)
            textSize = 20f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 36)
        }
        root.addView(title)

        val gridLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val rows = listOf(
            intArrayOf(ThemeManager.THEME_GREEN, ThemeManager.THEME_BLUE, ThemeManager.THEME_ORANGE),
            intArrayOf(ThemeManager.THEME_PURPLE, ThemeManager.THEME_PINK, ThemeManager.THEME_TEAL)
        )

        for (rowThemes in rows) {
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 24)
            }

            for (themeIndex in rowThemes) {
                val item = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val color = ThemeManager.getThemePrimaryColorFor(themeIndex)
                val circle = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(56, 56).apply {
                        bottomMargin = 10
                    }
                    val bg = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(color)
                        if (themeIndex == currentTheme) {
                            setStroke(4, Color.DKGRAY)
                        }
                    }
                    background = bg
                }

                val label = TextView(context).apply {
                    text = getString(themeNames[themeIndex])
                    textSize = 13f
                    setTextColor(
                        if (themeIndex == currentTheme) Color.BLACK
                        else Color.GRAY
                    )
                    gravity = Gravity.CENTER
                }

                item.addView(circle)
                item.addView(label)
                item.setOnClickListener {
                    ThemeManager.applyColorTheme(context, themeIndex)
                    requireActivity().recreate()
                }
                row.addView(item)
            }
            gridLayout.addView(row)
        }
        root.addView(gridLayout)

        val cancelBtn = TextView(context).apply {
            text = getString(R.string.cancel)
            textSize = 15f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
            setOnClickListener {
                dialog.dismiss()
            }
        }
        root.addView(cancelBtn)

        return root
    }

    private fun showThemeModeDialog() {
        val currentMode = ThemeManager.getCurrentThemeMode(requireContext())
        val items = arrayOf(getString(R.string.theme_light), getString(R.string.theme_dark), getString(R.string.theme_system))

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_theme))
            .setSingleChoiceItems(items, currentMode) { dialog, which ->
                ThemeManager.applyThemeMode(requireContext(), which)
                updateAllDisplays()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showLanguageDialog() {
        val currentLang = ThemeManager.getCurrentLanguage(requireContext())
        val items = arrayOf(getString(R.string.lang_chinese), "English")
        val checkedItem = if (currentLang == ThemeManager.LANG_EN) 1 else 0

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_language))
            .setSingleChoiceItems(items, checkedItem) { dialog, which ->
                val langCode = if (which == 0) ThemeManager.LANG_ZH else ThemeManager.LANG_EN
                ThemeManager.applyLanguage(requireContext(), langCode)
                updateAllDisplays()
                dialog.dismiss()
                requireActivity().recreate()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun updateAllDisplays() {
        tvThemeValue.text = ThemeManager.getThemeModeDisplayName(requireContext())
        tvColorThemeValue.text = ThemeManager.getColorThemeName(requireContext())
        tvLanguageValue.text = ThemeManager.getLanguageDisplayName(requireContext())

        val colorCircle = vColorPreview.background as? GradientDrawable
        if (colorCircle == null) {
            val circle = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ThemeManager.getColorThemePrimaryColor(requireContext()))
            }
            vColorPreview.background = circle
        } else {
            colorCircle.setColor(ThemeManager.getColorThemePrimaryColor(requireContext()))
        }
    }
}
