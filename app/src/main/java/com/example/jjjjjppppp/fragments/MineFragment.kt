package com.example.jjjjjppppp.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.jjjjjppppp.R
import com.example.jjjjjppppp.utils.ThemeManager
import java.util.Calendar

class MineFragment : Fragment() {

    // Login views
    private lateinit var layoutLoginSection: LinearLayout
    private lateinit var cardLoginForm: CardView
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvLoginTitle: TextView
    private lateinit var tvLoginSubtitle: TextView

    // Profile header views
    private lateinit var layoutProfileSection: LinearLayout
    private lateinit var tvProfileName: TextView
    private lateinit var tvMemberBadge: TextView

    // Stats views
    private lateinit var tvStatFavorites: TextView
    private lateinit var tvStatFollowing: TextView
    private lateinit var tvStatFollowers: TextView
    private lateinit var tvStatPosts: TextView

    // Account info value views
    private lateinit var tvNicknameValue: TextView
    private lateinit var tvGenderValue: TextView
    private lateinit var tvBirthdayValue: TextView
    private lateinit var tvPhoneValue: TextView
    private lateinit var tvEmailValue: TextView
    private lateinit var tvAddressValue: TextView
    private lateinit var tvBioValue: TextView

    // Settings value views
    private lateinit var tvColorThemeValue: TextView
    private lateinit var vColorPreview: View
    private lateinit var tvDarkModeValue: TextView
    private lateinit var tvLanguageValue: TextView

    private var isLoggedIn = false
    private var currentUsername = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Login views
        layoutLoginSection = view.findViewById(R.id.layoutLoginSection)
        cardLoginForm = view.findViewById(R.id.cardLoginForm)
        etUsername = view.findViewById(R.id.etUsername)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnRegister = view.findViewById(R.id.btnRegister)
        tvLoginTitle = view.findViewById(R.id.tvLoginTitle)
        tvLoginSubtitle = view.findViewById(R.id.tvLoginSubtitle)

        // Profile header
        layoutProfileSection = view.findViewById(R.id.layoutProfileSection)
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvMemberBadge = view.findViewById(R.id.tvMemberBadge)

        // Stats
        tvStatFavorites = view.findViewById(R.id.tvStatFavorites)
        tvStatFollowing = view.findViewById(R.id.tvStatFollowing)
        tvStatFollowers = view.findViewById(R.id.tvStatFollowers)
        tvStatPosts = view.findViewById(R.id.tvStatPosts)

        // Account info values
        tvNicknameValue = view.findViewById(R.id.tvNicknameValue)
        tvGenderValue = view.findViewById(R.id.tvGenderValue)
        tvBirthdayValue = view.findViewById(R.id.tvBirthdayValue)
        tvPhoneValue = view.findViewById(R.id.tvPhoneValue)
        tvEmailValue = view.findViewById(R.id.tvEmailValue)
        tvAddressValue = view.findViewById(R.id.tvAddressValue)
        tvBioValue = view.findViewById(R.id.tvBioValue)

        // Settings values
        tvColorThemeValue = view.findViewById(R.id.tvColorThemeValue)
        vColorPreview = view.findViewById(R.id.vColorPreview)
        tvDarkModeValue = view.findViewById(R.id.tvDarkModeValue)
        tvLanguageValue = view.findViewById(R.id.tvLanguageValue)

        checkLoginState()
        setupLoginListeners()
        setupRowClickListeners(view)
        setupProfileListeners()
    }

    override fun onResume() {
        super.onResume()
        if (isLoggedIn) {
            refreshAllDisplays()
        }
    }

    // ==================== Login/Register ====================

    private fun checkLoginState() {
        val prefs = requireContext().getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
        isLoggedIn = prefs.getBoolean("is_logged_in", false)
        currentUsername = prefs.getString("current_user", "") ?: ""

        if (isLoggedIn && currentUsername.isNotEmpty()) {
            showProfileSection()
        } else {
            showLoginSection()
        }
    }

    private fun showLoginSection() {
        layoutLoginSection.visibility = View.VISIBLE
        layoutProfileSection.visibility = View.GONE
        tvLoginTitle.text = getString(R.string.login_title)
        tvLoginSubtitle.text = getString(R.string.login_subtitle)
        cardLoginForm.visibility = View.VISIBLE
        btnLogin.visibility = View.VISIBLE
        btnRegister.text = getString(R.string.register_new_account)
    }

    private fun showProfileSection() {
        layoutLoginSection.visibility = View.GONE
        layoutProfileSection.visibility = View.VISIBLE
        tvProfileName.text = currentUsername
        tvMemberBadge.text = getString(R.string.member_regular)
        refreshAllDisplays()
    }

    private fun setupLoginListeners() {
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.enter_username_password), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = requireContext().getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
            val users = prefs.getStringSet("registered_users", emptySet()) ?: emptySet()

            if (!users.contains(username)) {
                Toast.makeText(requireContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedPassword = prefs.getString("pwd_$username", "")
            if (password != savedPassword) {
                Toast.makeText(requireContext(), getString(R.string.wrong_password), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit().apply {
                putBoolean("is_logged_in", true)
                putString("current_user", username)
                apply()
            }
            isLoggedIn = true
            currentUsername = username
            showProfileSection()
            Toast.makeText(requireContext(), getString(R.string.login_success, username), Toast.LENGTH_SHORT).show()
        }

        btnRegister.setOnClickListener {
            showRegisterDialog()
        }
    }

    private fun setupProfileListeners() {
        view?.findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
            logout()
        }
    }

    private fun showRegisterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_register, null)
        val etRegUsername = dialogView.findViewById<EditText>(R.id.etRegUsername)
        val etRegPassword = dialogView.findViewById<EditText>(R.id.etRegPassword)
        val etRegConfirmPassword = dialogView.findViewById<EditText>(R.id.etRegConfirmPassword)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(getString(R.string.register_btn)) { _, _ ->
                val username = etRegUsername.text.toString().trim()
                val password = etRegPassword.text.toString().trim()
                val confirmPassword = etRegConfirmPassword.text.toString().trim()

                if (username.length < 3) {
                    Toast.makeText(requireContext(), getString(R.string.username_too_short), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (password.length < 4) {
                    Toast.makeText(requireContext(), getString(R.string.password_too_short), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (password != confirmPassword) {
                    Toast.makeText(requireContext(), getString(R.string.password_mismatch), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val prefs = requireContext().getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
                val users = prefs.getStringSet("registered_users", emptySet())?.toMutableSet() ?: mutableSetOf()

                if (users.contains(username)) {
                    Toast.makeText(requireContext(), getString(R.string.username_exists), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                users.add(username)
                prefs.edit().apply {
                    putStringSet("registered_users", users)
                    putString("pwd_$username", password)
                    putBoolean("is_logged_in", true)
                    putString("current_user", username)
                    apply()
                }
                isLoggedIn = true
                currentUsername = username
                showProfileSection()
                Toast.makeText(requireContext(), getString(R.string.register_success), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun logout() {
        val prefs = requireContext().getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_logged_in", false)
            remove("current_user")
            apply()
        }
        isLoggedIn = false
        currentUsername = ""
        showLoginSection()
        etUsername.text.clear()
        etPassword.text.clear()
        Toast.makeText(requireContext(), getString(R.string.logged_out), Toast.LENGTH_SHORT).show()
    }

    // ==================== Display Refresh ====================

    private fun refreshAllDisplays() {
        // Profile
        tvProfileName.text = currentUsername

        // Account info
        val profilePrefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        tvNicknameValue.text = profilePrefs.getString("nickname", "")?.ifEmpty { null } ?: ""
        tvGenderValue.text = when (profilePrefs.getInt("gender", 0)) {
            1 -> getString(R.string.gender_male)
            2 -> getString(R.string.gender_female)
            else -> ""
        }
        tvBirthdayValue.text = profilePrefs.getString("birthday", "")?.ifEmpty { null } ?: ""
        tvPhoneValue.text = profilePrefs.getString("phone", "")?.ifEmpty { null } ?: ""
        tvEmailValue.text = profilePrefs.getString("email", "")?.ifEmpty { null } ?: ""
        tvAddressValue.text = profilePrefs.getString("address", "")?.ifEmpty { null } ?: ""
        tvBioValue.text = profilePrefs.getString("bio", "")?.ifEmpty { null } ?: ""

        // Stats
        val statsPrefs = requireContext().getSharedPreferences("profile_stats", Context.MODE_PRIVATE)
        tvStatFavorites.text = statsPrefs.getInt("stat_favorites", 0).toString()
        tvStatFollowing.text = statsPrefs.getInt("stat_following", 0).toString()
        tvStatFollowers.text = statsPrefs.getInt("stat_followers", 0).toString()
        tvStatPosts.text = statsPrefs.getInt("stat_posts", 0).toString()

        // Settings
        tvColorThemeValue.text = ThemeManager.getColorThemeName(requireContext())
        tvDarkModeValue.text = ThemeManager.getThemeModeDisplayName(requireContext())
        tvLanguageValue.text = ThemeManager.getLanguageDisplayName(requireContext())

        // Color preview circle
        val primaryColor = ThemeManager.getColorThemePrimaryColor(requireContext())
        val circle = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(primaryColor)
        }
        vColorPreview.background = circle
    }

    // ==================== Row Click Handlers ====================

    private fun setupRowClickListeners(view: View) {
        // Account info rows
        view.findViewById<View>(R.id.rowNickname).setOnClickListener {
            showEditTextDialog(getString(R.string.label_nickname), tvNicknameValue, "nickname", R.string.saved_nickname)
        }
        view.findViewById<View>(R.id.rowGender).setOnClickListener { showGenderDialog() }
        view.findViewById<View>(R.id.rowBirthday).setOnClickListener { showDatePickerDialog() }
        view.findViewById<View>(R.id.rowPhone).setOnClickListener {
            showEditTextDialog(getString(R.string.label_phone), tvPhoneValue, "phone", R.string.saved_phone)
        }
        view.findViewById<View>(R.id.rowEmail).setOnClickListener {
            showEditTextDialog(getString(R.string.label_email), tvEmailValue, "email", R.string.saved_email)
        }
        view.findViewById<View>(R.id.rowAddress).setOnClickListener {
            showEditTextDialog(getString(R.string.label_address), tvAddressValue, "address", R.string.saved_address)
        }
        view.findViewById<View>(R.id.rowBio).setOnClickListener {
            showEditTextDialog(getString(R.string.label_bio), tvBioValue, "bio", R.string.saved_bio)
        }

        // Settings rows
        view.findViewById<View>(R.id.rowColorTheme).setOnClickListener { showColorThemeDialog() }
        view.findViewById<View>(R.id.rowDarkMode).setOnClickListener { showThemeModeDialog() }
        view.findViewById<View>(R.id.rowLanguage).setOnClickListener { showLanguageDialog() }

        // Other rows
        view.findViewById<View>(R.id.rowClearCache).setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.rowCheckUpdate).setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.latest_version), Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.rowAbout).setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.about_info), Toast.LENGTH_LONG).show()
        }
        view.findViewById<View>(R.id.rowFeedback).setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.thanks_feedback), Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.rowPrivacy).setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.privacy_policy_text), Toast.LENGTH_LONG).show()
        }
    }

    // ==================== Edit Dialogs ====================

    private fun showEditTextDialog(title: String, targetTv: TextView, prefKey: String, successMsgId: Int) {
        val input = EditText(requireContext()).apply {
            setText(targetTv.text)
            textSize = 16f
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val value = input.text.toString().trim()
                requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                    .edit().putString(prefKey, value).apply()
                targetTv.text = value
                Toast.makeText(requireContext(), getString(successMsgId), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showGenderDialog() {
        val genders = arrayOf(getString(R.string.gender_male), getString(R.string.gender_female), getString(R.string.gender_unspecified))
        val profilePrefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val current = profilePrefs.getInt("gender", 0)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_gender))
            .setSingleChoiceItems(genders, if (current == 1) 0 else if (current == 2) 1 else 2) { dialog, which ->
                val genderValue = when (which) {
                    0 -> 1
                    1 -> 2
                    else -> 0
                }
                profilePrefs.edit().putInt("gender", genderValue).apply()
                tvGenderValue.text = when (genderValue) {
                    1 -> getString(R.string.gender_male)
                    2 -> getString(R.string.gender_female)
                    else -> ""
                }
                Toast.makeText(requireContext(), getString(R.string.saved_gender), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showDatePickerDialog() {
        val cal = Calendar.getInstance()

        // Parse existing date if set
        val existingDate = tvBirthdayValue.text.toString()
        if (existingDate.isNotEmpty()) {
            val parts = existingDate.split("-")
            if (parts.size == 3) {
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        }

        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val formatted = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
            requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                .edit().putString("birthday", formatted).apply()
            tvBirthdayValue.text = formatted
            Toast.makeText(requireContext(), getString(R.string.saved_birthday), Toast.LENGTH_SHORT).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    // ==================== Settings Dialogs ====================

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
        val themeNames = intArrayOf(
            R.string.theme_blue, R.string.theme_green, R.string.theme_orange,
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
            intArrayOf(ThemeManager.THEME_BLUE, ThemeManager.THEME_GREEN, ThemeManager.THEME_ORANGE),
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
                refreshAllDisplays()
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
                refreshAllDisplays()
                dialog.dismiss()
                requireActivity().recreate()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
