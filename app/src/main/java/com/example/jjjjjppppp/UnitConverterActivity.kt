package com.example.jjjjjppppp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UnitConverterActivity : AppCompatActivity() {
    private lateinit var etInput: EditText
    private lateinit var tvResult: TextView

    private val categories = arrayOf("长度", "重量", "温度")
    private val lengthUnits = arrayOf("米 → 厘米", "厘米 → 米", "米 → 千米", "千米 → 米", "英尺 → 米", "米 → 英尺")
    private val weightUnits = arrayOf("千克 → 克", "克 → 千克", "千克 → 磅", "磅 → 千克")
    private val tempUnits = arrayOf("摄氏度 → 华氏度", "华氏度 → 摄氏度")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unit_converter)

        etInput = findViewById(R.id.etInput)
        tvResult = findViewById(R.id.tvResult)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)

        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                convert()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        etInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { convert() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun convert() {
        val input = etInput.text.toString().toDoubleOrNull()
        if (input == null) {
            tvResult.text = "请输入有效数值"
            return
        }

        when (findViewById<Spinner>(R.id.spinnerCategory).selectedItemPosition) {
            0 -> tvResult.text = convertLength(input)
            1 -> tvResult.text = convertWeight(input)
            2 -> tvResult.text = convertTemperature(input)
        }
    }

    private fun convertLength(value: Double): String {
        return "厘米: ${value * 100}\n千米: ${value / 1000}\n英尺: ${value * 3.28084}\n英寸: ${value * 39.3701}"
    }

    private fun convertWeight(value: Double): String {
        return "克: ${value * 1000}\n磅: ${value * 2.20462}\n盎司: ${value * 35.274}"
    }

    private fun convertTemperature(value: Double): String {
        return "华氏度: ${value * 9 / 5 + 32}\n开尔文: ${value + 273.15}"
    }
}
