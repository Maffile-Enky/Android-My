package com.example.jjjjjppppp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class RandomNumberActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random_number)

        val tvResult = findViewById<TextView>(R.id.tvResult)
        val etMin = findViewById<EditText>(R.id.etMin)
        val etMax = findViewById<EditText>(R.id.etMax)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        val btnDice = findViewById<Button>(R.id.btnDice)

        btnGenerate.setOnClickListener {
            val min = etMin.text.toString().toIntOrNull() ?: 1
            val max = etMax.text.toString().toIntOrNull() ?: 100
            if (min < max) {
                val result = Random.nextInt(min, max + 1)
                tvResult.text = result.toString()
            }
        }

        btnDice.setOnClickListener {
            val result = Random.nextInt(1, 7)
            tvResult.text = result.toString()
        }
    }
}
