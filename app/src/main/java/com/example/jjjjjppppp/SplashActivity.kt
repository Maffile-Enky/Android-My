package com.example.jjjjjppppp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.jjjjjppppp.network.RetrofitClient

class SplashActivity : AppCompatActivity() {

    private var keepSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplash }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        RetrofitClient.init(this)

        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        logoImageView.startAnimation(animation)

        Handler(Looper.getMainLooper()).postDelayed({
            keepSplash = false
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }
}
