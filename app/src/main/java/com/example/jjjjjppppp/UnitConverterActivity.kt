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

    private val categories by lazy { arrayOf(getString(R.string.category_length), getString(R.string.category_weight), getString(R.string.category_temperature)) }
    private val lengthUnits by lazy { getString(R.string.length_options).split(",").toTypedArray() }
    private val weightUnits by lazy { getString(R.string.weight_options).split(",").toTypedArray() }
    private val tempUnits by lazy { getString(R.string.temperature_options).split(",").toTypedArray() }

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
            tvResult.text = getString(R.string.enter_valid_value)
            return
        }

        when (findViewById<Spinner>(R.id.spinnerCategory).selectedItemPosition) {
            0 -> tvResult.text = convertLength(input)
            1 -> tvResult.text = convertWeight(input)
            2 -> tvResult.text = convertTemperature(input)
        }
    }

    private fun convertLength(value: Double): String {
        return "${getString(R.string.unit_cm)}: ${value * 100}\n" +
                "${getString(R.string.unit_km)}: ${value / 1000}\n" +
                "${getString(R.string.unit_ft)}: ${value * 3.28084}\n" +
                "${getString(R.string.unit_in)}: ${value * 39.3701}"
    }

    private fun convertWeight(value: Double): String {
        return "${getString(R.string.unit_g)}: ${value * 1000}\n" +
                "${getString(R.string.unit_lb)}: ${value * 2.20462}\n" +
                "${getString(R.string.unit_oz)}: ${value * 35.274}"
    }

    private fun convertTemperature(value: Double): String {
        return "${getString(R.string.unit_fahrenheit)}: ${value * 9 / 5 + 32}\n" +
                "${getString(R.string.unit_kelvin)}: ${value + 273.15}"
    }
}
