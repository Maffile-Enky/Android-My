package com.example.jjjjjppppp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.jjjjjppppp.R
import com.example.jjjjjppppp.utils.ThemeManager

class SettingsFragment : Fragment() {

    private lateinit var tvThemeValue: TextView
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
        tvLanguageValue = view.findViewById(R.id.tvLanguageValue)
        updateThemeDisplay()
        updateLanguageDisplay()

        view.findViewById<View>(R.id.llTheme).setOnClickListener { showThemeDialog() }
        view.findViewById<View>(R.id.llLanguage).setOnClickListener { showLanguageDialog() }
        view.findViewById<View>(R.id.llClearCache).setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.llCheckUpdate).setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.latest_version), Toast.LENGTH_SHORT).show()
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

    private fun showThemeDialog() {
        val currentMode = ThemeManager.getCurrentTheme(requireContext())
        val items = arrayOf(getString(R.string.theme_light), getString(R.string.theme_dark), getString(R.string.theme_system))
        val checkedItem = currentMode

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_theme))
            .setSingleChoiceItems(items, checkedItem) { dialog, which ->
                ThemeManager.applyTheme(requireContext(), which)
                updateThemeDisplay()
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
                updateLanguageDisplay()
                dialog.dismiss()
                requireActivity().recreate()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun updateThemeDisplay() {
        tvThemeValue.text = ThemeManager.getThemeDisplayName(requireContext())
    }

    private fun updateLanguageDisplay() {
        tvLanguageValue.text = ThemeManager.getLanguageDisplayName(requireContext())
    }
}
