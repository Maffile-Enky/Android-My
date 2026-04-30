package com.example.jjjjjppppp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CalculatorActivity : BaseActivity() {

    private lateinit var tvDisplay: TextView
    private var currentNumber: StringBuilder = StringBuilder()
    private var previousNumber: Double = 0.0
    private var operation: String? = null
    private var isNewOperation: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        tvDisplay = findViewById(R.id.tvDisplay)
        setupNumberButtons()
        setupOperationButtons()
    }

    private fun setupNumberButtons() {
        val numberButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
            R.id.btn8, R.id.btn9, R.id.btnDot
        )

        for (btnId in numberButtons) {
            findViewById<Button>(btnId).setOnClickListener { view ->
                onNumberClick((view as Button).text.toString())
            }
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            onClearClick()
        }

        findViewById<Button>(R.id.btnBackspace).setOnClickListener {
            onBackspaceClick()
        }

        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            onEqualsClick()
        }
    }

    private fun setupOperationButtons() {
        val operationButtons = mapOf(
            R.id.btnAdd to "+",
            R.id.btnSubtract to "-",
            R.id.btnMultiply to "×",
            R.id.btnDivide to "÷"
        )

        for ((btnId, op) in operationButtons) {
            findViewById<Button>(btnId).setOnClickListener {
                onOperationClick(op)
            }
        }
    }

    private fun onNumberClick(number: String) {
        if (isNewOperation) {
            currentNumber = StringBuilder()
            isNewOperation = false
        }

        // 处理小数点
        if (number == ".") {
            if (!currentNumber.contains(".")) {
                currentNumber.append(".")
            }
        } else {
            currentNumber.append(number)
        }

        updateDisplay()
    }

    private fun onOperationClick(op: String) {
        if (currentNumber.isNotEmpty()) {
            if (previousNumber != 0.0 && operation != null && !isNewOperation) {
                calculateResult()
            }
            previousNumber = currentNumber.toString().toDoubleOrNull() ?: 0.0
            operation = op
            isNewOperation = true
        }
    }

    private fun onEqualsClick() {
        if (currentNumber.isNotEmpty() && operation != null) {
            calculateResult()
            operation = null
        }
    }

    private fun onClearClick() {
        currentNumber = StringBuilder()
        previousNumber = 0.0
        operation = null
        isNewOperation = true
        updateDisplay()
    }

    private fun onBackspaceClick() {
        if (currentNumber.isNotEmpty()) {
            currentNumber.deleteCharAt(currentNumber.length - 1)
            updateDisplay()
        }
    }

    private fun calculateResult() {
        val current = currentNumber.toString().toDoubleOrNull() ?: 0.0
        var result: Double = 0.0

        when (operation) {
            "+" -> result = previousNumber + current
            "-" -> result = previousNumber - current
            "×" -> result = previousNumber * current
            "÷" -> result = if (current != 0.0) previousNumber / current else 0.0
        }

        currentNumber = StringBuilder()
        if (result == result.toLong().toDouble()) {
            currentNumber.append(result.toLong().toString())
        } else {
            currentNumber.append(result.toString())
        }

        previousNumber = 0.0
        isNewOperation = true
        updateDisplay()
    }

    private fun updateDisplay() {
        tvDisplay.text = if (currentNumber.isEmpty()) "0" else currentNumber.toString()
    }
}