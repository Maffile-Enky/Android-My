package com.example.jjjjjppppp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NotesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        val etTitle = findViewById<EditText>(R.id.etNoteTitle)
        val etContent = findViewById<EditText>(R.id.etNoteContent)
        val btnSave = findViewById<Button>(R.id.btnSaveNote)
        val btnLoad = findViewById<Button>(R.id.btnLoadNote)
        val btnClear = findViewById<Button>(R.id.btnClearNote)

        val prefs = getSharedPreferences("notes_prefs", Context.MODE_PRIVATE)

        btnSave.setOnClickListener {
            prefs.edit().apply {
                putString("note_title", etTitle.text.toString())
                putString("note_content", etContent.text.toString())
                apply()
            }
            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show()
        }

        btnLoad.setOnClickListener {
            etTitle.setText(prefs.getString("note_title", ""))
            etContent.setText(prefs.getString("note_content", ""))
            Toast.makeText(this, "已加载", Toast.LENGTH_SHORT).show()
        }

        btnClear.setOnClickListener {
            etTitle.text.clear()
            etContent.text.clear()
        }
    }
}
