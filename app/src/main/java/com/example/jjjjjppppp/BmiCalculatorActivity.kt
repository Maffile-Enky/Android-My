package com.example.jjjjjppppp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BmiCalculatorActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi_calculator)

        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val tvBmiValue = findViewById<TextView>(R.id.tvBmiValue)
        val tvBmiCategory = findViewById<TextView>(R.id.tvBmiCategory)
        val tvBmiRange = findViewById<TextView>(R.id.tvBmiRange)

        btnCalculate.setOnClickListener {
            val heightCm = etHeight.text.toString().toDoubleOrNull()
            val weightKg = etWeight.text.toString().toDoubleOrNull()

            if (heightCm == null || weightKg == null || heightCm <= 0 || weightKg <= 0) {
                tvBmiValue.text = "-"
                tvBmiCategory.text = getString(R.string.enter_valid_height_weight)
                tvBmiRange.text = ""
                return@setOnClickListener
            }

            val heightM = heightCm / 100
            val bmi = weightKg / (heightM * heightM)
            val (category, range) = getBmiCategory(bmi)

            tvBmiValue.text = String.format("%.1f", bmi)
            tvBmiCategory.text = category
            tvBmiRange.text = getString(R.string.normal_range, range)
        }
    }

    private fun getBmiCategory(bmi: Double): Pair<String, String> {
        return when {
            bmi < 18.5 -> getString(R.string.underweight) to "18.5 - 24.9"
            bmi < 24.9 -> getString(R.string.normal) to "18.5 - 24.9"
            bmi < 29.9 -> getString(R.string.overweight) to "18.5 - 24.9"
            else -> getString(R.string.obese) to "18.5 - 24.9"
        }
    }
}
