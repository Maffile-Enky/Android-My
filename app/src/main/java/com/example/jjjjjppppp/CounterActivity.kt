package com.example.jjjjjppppp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CounterActivity : BaseActivity() {

    private lateinit var tvCount: TextView
    private lateinit var btnIncrease: Button
    private lateinit var btnDecrease: Button
    private lateinit var btnReset: Button

    private var count: Int = 0
    private val PREFS_NAME = "CounterPrefs"
    private val KEY_COUNT = "count"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counter)

        // 初始化视图
        initViews()

        // 加载保存的计数值
        loadCount()

        // 设置按钮点击事件
        setupClickListeners()
    }

    private fun initViews() {
        tvCount = findViewById(R.id.tvCount)
        btnIncrease = findViewById(R.id.btnIncrease)
        btnDecrease = findViewById(R.id.btnDecrease)
        btnReset = findViewById(R.id.btnReset)
    }

    private fun loadCount() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        count = prefs.getInt(KEY_COUNT, 0)
        updateDisplay()
    }

    private fun saveCount() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt(KEY_COUNT, count)
        editor.apply()
    }

    private fun setupClickListeners() {
        btnIncrease.setOnClickListener {
            count++
            updateDisplay()
            saveCount()
        }

        btnDecrease.setOnClickListener {
            count--
            updateDisplay()
            saveCount()
        }

        btnReset.setOnClickListener {
            count = 0
            updateDisplay()
            saveCount()
        }
    }

    private fun updateDisplay() {
        tvCount.text = count.toString()

        // 根据计数值改变颜色
        val color = when {
            count > 0 -> getColor(R.color.positive_count)
            count < 0 -> getColor(R.color.negative_count)
            else -> getColor(R.color.counter_text)
        }
        tvCount.setTextColor(color)
    }
}