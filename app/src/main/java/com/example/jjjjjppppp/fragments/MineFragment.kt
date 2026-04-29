package com.example.jjjjjppppp.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.jjjjjppppp.R

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

    // Profile views
    private lateinit var layoutProfileSection: LinearLayout
    private lateinit var tvWelcomeHeader: TextView
    private lateinit var etNickname: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var etBirthday: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAddress: EditText
    private lateinit var etBio: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var btnLogout: Button

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

        // Profile views
        layoutProfileSection = view.findViewById(R.id.layoutProfileSection)
        tvWelcomeHeader = view.findViewById(R.id.tvWelcomeHeader)
        etNickname = view.findViewById(R.id.etNickname)
        rgGender = view.findViewById(R.id.rgGender)
        etBirthday = view.findViewById(R.id.etBirthday)
        etPhone = view.findViewById(R.id.etPhone)
        etEmail = view.findViewById(R.id.etEmail)
        etAddress = view.findViewById(R.id.etAddress)
        etBio = view.findViewById(R.id.etBio)
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)
        btnLogout = view.findViewById(R.id.btnLogout)

        checkLoginState()
        setupLoginListeners()
        setupProfileListeners()
    }

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
        tvWelcomeHeader.text = getString(R.string.logged_in_welcome, currentUsername)
        loadProfile()
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
            if (isLoggedIn) {
                logout()
            } else {
                showRegisterDialog()
            }
        }
    }

    private fun setupProfileListeners() {
        btnSaveProfile.setOnClickListener {
            saveProfile()
            Toast.makeText(requireContext(), getString(R.string.profile_saved), Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            logout()
        }
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

    private fun loadProfile() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        etNickname.setText(prefs.getString("nickname", ""))
        val gender = prefs.getInt("gender", 0)
        if (gender == 1) rgGender.check(R.id.rbMale)
        else if (gender == 2) rgGender.check(R.id.rbFemale)
        else rgGender.clearCheck()
        etBirthday.setText(prefs.getString("birthday", ""))
        etPhone.setText(prefs.getString("phone", ""))
        etEmail.setText(prefs.getString("email", ""))
        etAddress.setText(prefs.getString("address", ""))
        etBio.setText(prefs.getString("bio", ""))
    }

    private fun saveProfile() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("nickname", etNickname.text.toString())
            putInt("gender", when (rgGender.checkedRadioButtonId) {
                R.id.rbMale -> 1
                R.id.rbFemale -> 2
                else -> 0
            })
            putString("birthday", etBirthday.text.toString())
            putString("phone", etPhone.text.toString())
            putString("email", etEmail.text.toString())
            putString("address", etAddress.text.toString())
            putString("bio", etBio.text.toString())
            apply()
        }
    }
}
