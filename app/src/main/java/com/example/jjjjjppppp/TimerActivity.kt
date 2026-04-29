package com.example.jjjjjppppp

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class TimerActivity : AppCompatActivity() {

    private lateinit var tvTime: TextView
    private lateinit var btnStart: Button
    private lateinit var btnSet: Button
    private lateinit var btnReset: Button

    private var countDownTimer: CountDownTimer? = null
    private var timeInMillis: Long = 60000 // 默认1分钟
    private var timeLeft: Long = timeInMillis
    private var isTimerRunning: Boolean = false
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        tvTime = findViewById(R.id.tvTime)
        btnStart = findViewById(R.id.btnStart)
        btnSet = findViewById(R.id.btnSet)
        btnReset = findViewById(R.id.btnReset)

        updateTimeDisplay(timeLeft)
        setupButtons()
    }

    private fun setupButtons() {
        btnStart.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        btnSet.setOnClickListener {
            showTimePicker()
        }

        btnReset.setOnClickListener {
            resetTimer()
        }
    }

    private fun startTimer() {
        if (timeLeft <= 0) {
            Toast.makeText(this, getString(R.string.please_set_time), Toast.LENGTH_SHORT).show()
            return
        }

        countDownTimer = object : CountDownTimer(timeLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                updateTimeDisplay(timeLeft)
            }

            override fun onFinish() {
                timeLeft = 0
                updateTimeDisplay(timeLeft)
                isTimerRunning = false
                btnStart.text = getString(R.string.start)
                btnStart.backgroundTintList = ColorStateList.valueOf(getColor(R.color.increase_btn))
                playAlarm()
            }
        }.start()

        isTimerRunning = true
        btnStart.text = getString(R.string.pause)
        btnStart.backgroundTintList = ColorStateList.valueOf(getColor(R.color.negative_count))
        btnSet.isEnabled = false
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnStart.text = getString(R.string.resume)
        btnStart.backgroundTintList = ColorStateList.valueOf(getColor(R.color.increase_btn))
        btnSet.isEnabled = true
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        timeLeft = timeInMillis
        updateTimeDisplay(timeLeft)
        isTimerRunning = false
        btnStart.text = getString(R.string.start)
        btnStart.backgroundTintList = ColorStateList.valueOf(getColor(R.color.increase_btn))
        btnSet.isEnabled = true
    }

    private fun showTimePicker() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null)
        val npMinutes = dialogView.findViewById<NumberPicker>(R.id.npMinutes)
        val npSeconds = dialogView.findViewById<NumberPicker>(R.id.npSeconds)

        npMinutes.minValue = 0
        npMinutes.maxValue = 99
        npMinutes.value = (timeInMillis / 60000).toInt()

        npSeconds.minValue = 0
        npSeconds.maxValue = 59
        npSeconds.value = ((timeInMillis % 60000) / 1000).toInt()

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.set_time))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                timeInMillis = (npMinutes.value * 60000 + npSeconds.value * 1000).toLong()
                timeLeft = timeInMillis
                updateTimeDisplay(timeLeft)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun updateTimeDisplay(time: Long) {
        val minutes = time / 60000
        val seconds = (time % 60000) / 1000
        tvTime.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun playAlarm() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
            }
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.time_up), Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}