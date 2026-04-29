package com.example.jjjjjppppp.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.jjjjjppppp.R
import com.example.jjjjjppppp.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private var isLoggedIn = false
    private var currentUsername = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkLoginState()
        setupListeners()
    }

    private fun checkLoginState() {
        val prefs = requireContext().getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
        isLoggedIn = prefs.getBoolean("is_logged_in", false)
        currentUsername = prefs.getString("current_user", "") ?: ""

        if (isLoggedIn && currentUsername.isNotEmpty()) {
            showLoggedInState()
        }
    }

    private fun showLoggedInState() {
        binding.tvLoginTitle.text = getString(R.string.logged_in_label)
        binding.tvLoginSubtitle.text = getString(R.string.logged_in_welcome, currentUsername)
        binding.cardLoginForm.visibility = View.GONE
        binding.btnLogin.visibility = View.GONE
        binding.btnRegister.text = getString(R.string.logout)
    }

    private fun showLoginForm() {
        binding.tvLoginTitle.text = getString(R.string.login_title)
        binding.tvLoginSubtitle.text = getString(R.string.login_subtitle)
        binding.cardLoginForm.visibility = View.VISIBLE
        binding.btnLogin.visibility = View.VISIBLE
        binding.btnRegister.text = getString(R.string.register_new_account)
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

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

            // Login success
            prefs.edit().apply {
                putBoolean("is_logged_in", true)
                putString("current_user", username)
                apply()
            }
            isLoggedIn = true
            currentUsername = username
            showLoggedInState()
            Toast.makeText(requireContext(), getString(R.string.login_success, username), Toast.LENGTH_SHORT).show()
        }

        binding.btnRegister.setOnClickListener {
            if (isLoggedIn) {
                logout()
            } else {
                showRegisterDialog()
            }
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
        showLoginForm()
        binding.etUsername.text.clear()
        binding.etPassword.text.clear()
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
                showLoggedInState()
                Toast.makeText(requireContext(), getString(R.string.register_success), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
