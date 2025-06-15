package edu.praktis.org.newtestapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 1000 // Durasi splash screen dalam milidetik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Pastikan nama layout sesuai

        Handler(Looper.getMainLooper()).postDelayed({
            // Intent untuk memulai MainActivity
            startActivity(Intent(this, LoginActivity::class.java))
            // Tutup activity ini
            finish()
        }, SPLASH_TIME_OUT)
    }
}