package com.example.jjjjjppppp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WordCounterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_counter)

        val etText = findViewById<EditText>(R.id.etTextInput)
        val tvCharCount = findViewById<TextView>(R.id.tvCharCount)
        val tvChineseCount = findViewById<TextView>(R.id.tvChineseCount)
        val tvWordCount = findViewById<TextView>(R.id.tvWordCount)
        val btnClear = findViewById<Button>(R.id.btnClear)

        etText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                tvCharCount.text = text.length.toString()
                tvChineseCount.text = text.count { it in '一'..'鿿' }.toString()
                tvWordCount.text = if (text.isBlank()) "0" else text.trim().split("\\s+".toRegex()).size.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnClear.setOnClickListener { etText.text.clear() }
    }
}
