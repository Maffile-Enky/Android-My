package com.example.jjjjjppppp.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.jjjjjppppp.R

class ProfileFragment : Fragment() {

    private lateinit var etNickname: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var etBirthday: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAddress: EditText
    private lateinit var etBio: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etNickname = view.findViewById(R.id.etNickname)
        rgGender = view.findViewById(R.id.rgGender)
        etBirthday = view.findViewById(R.id.etBirthday)
        etPhone = view.findViewById(R.id.etPhone)
        etEmail = view.findViewById(R.id.etEmail)
        etAddress = view.findViewById(R.id.etAddress)
        etBio = view.findViewById(R.id.etBio)
        val btnSave = view.findViewById<Button>(R.id.btnSaveProfile)

        loadProfile()

        btnSave.setOnClickListener {
            saveProfile()
            Toast.makeText(requireContext(), "个人信息已保存", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfile() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        etNickname.setText(prefs.getString("nickname", ""))
        val gender = prefs.getInt("gender", 0)
        if (gender == 1) rgGender.check(R.id.rbMale) else if (gender == 2) rgGender.check(R.id.rbFemale)
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
