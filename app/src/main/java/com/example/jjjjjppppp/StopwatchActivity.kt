package com.example.jjjjjppppp

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StopwatchActivity : BaseActivity() {

    private lateinit var tvTime: TextView
    private lateinit var tvLap: TextView
    private lateinit var btnStart: Button
    private lateinit var btnLap: Button
    private lateinit var btnReset: Button

    private val handler = Handler(Looper.getMainLooper())
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var isRunning: Boolean = false
    private var lapCount: Int = 0

    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                updateTimeDisplay(elapsedTime)
                handler.postDelayed(this, 10)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stopwatch)

        tvTime = findViewById(R.id.tvTime)
        tvLap = findViewById(R.id.tvLap)
        btnStart = findViewById(R.id.btnStart)
        btnLap = findViewById(R.id.btnLap)
        btnReset = findViewById(R.id.btnReset)

        setupButtons()
    }

    private fun setupButtons() {
        btnStart.setOnClickListener {
            if (isRunning) {
                pause()
            } else {
                start()
            }
        }

        btnLap.setOnClickListener {
            if (isRunning) {
                recordLap()
            }
        }

        btnReset.setOnClickListener {
            reset()
        }
    }

    private fun start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            isRunning = true
            handler.post(runnable)
            btnStart.text = getString(R.string.pause)
            btnStart.backgroundTintList = ColorStateList.valueOf(getColor(R.color.negative_count))
        }
    }

    private fun pause() {
        if (isRunning) {
            isRunning = false
            handler.removeCallbacks(runnable)
            btnStart.text = getString(R.string.start)
            btnStart.backgroundTintList = ColorStateList.valueOf(getColor(R.color.increase_btn))
        }
    }

    private fun recordLap() {
        lapCount++
        val lapText = getString(R.string.lap_format, lapCount, formatTime(elapsedTime))
        tvLap.text = if (tvLap.text.isEmpty()) lapText else "$lapText\n${tvLap.text}"
    }

    private fun reset() {
        pause()
        elapsedTime = 0
        lapCount = 0
        updateTimeDisplay(0)
        tvLap.text = ""
    }

    private fun updateTimeDisplay(time: Long) {
        tvTime.text = formatTime(time)
    }

    private fun formatTime(time: Long): String {
        val hours = time / 3600000
        val minutes = (time % 3600000) / 60000
        val seconds = (time % 60000) / 1000
        val milliseconds = time % 1000

        return if (hours > 0) {
            String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds)
        } else {
            String.format("%02d:%02d.%03d", minutes, seconds, milliseconds)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}