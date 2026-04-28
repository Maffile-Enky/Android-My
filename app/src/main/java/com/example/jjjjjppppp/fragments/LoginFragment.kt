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
        binding.tvLoginTitle.text = "已登录"
        binding.tvLoginSubtitle.text = "欢迎回来，$currentUsername"
        binding.cardLoginForm.visibility = View.GONE
        binding.btnLogin.visibility = View.GONE
        binding.btnRegister.text = "退出登录"
    }

    private fun showLoginForm() {
        binding.tvLoginTitle.text = "用户登录"
        binding.tvLoginSubtitle.text = "欢迎回来，请登录您的账号"
        binding.cardLoginForm.visibility = View.VISIBLE
        binding.btnLogin.visibility = View.VISIBLE
        binding.btnRegister.text = "注册新账号"
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = requireContext().getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
            val users = prefs.getStringSet("registered_users", emptySet()) ?: emptySet()

            if (!users.contains(username)) {
                Toast.makeText(requireContext(), "用户不存在，请先注册", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedPassword = prefs.getString("pwd_$username", "")
            if (password != savedPassword) {
                Toast.makeText(requireContext(), "密码错误", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(), "登录成功，欢迎 $username", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show()
    }

    private fun showRegisterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_register, null)
        val etRegUsername = dialogView.findViewById<EditText>(R.id.etRegUsername)
        val etRegPassword = dialogView.findViewById<EditText>(R.id.etRegPassword)
        val etRegConfirmPassword = dialogView.findViewById<EditText>(R.id.etRegConfirmPassword)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("注册") { _, _ ->
                val username = etRegUsername.text.toString().trim()
                val password = etRegPassword.text.toString().trim()
                val confirmPassword = etRegConfirmPassword.text.toString().trim()

                if (username.length < 3) {
                    Toast.makeText(requireContext(), "用户名至少需要3个字符", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (password.length < 4) {
                    Toast.makeText(requireContext(), "密码至少需要4个字符", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (password != confirmPassword) {
                    Toast.makeText(requireContext(), "两次密码输入不一致", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val prefs = requireContext().getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
                val users = prefs.getStringSet("registered_users", emptySet())?.toMutableSet() ?: mutableSetOf()

                if (users.contains(username)) {
                    Toast.makeText(requireContext(), "用户名已存在，请更换", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "注册成功，已自动登录", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
