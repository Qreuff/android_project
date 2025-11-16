package com.example.android_app

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android_app.MediaPlayerActivity

class main_menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)
        animation_text()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        animation_text()

        val GoToCalculator: Button = findViewById(R.id.bGoToCalculator)
        GoToCalculator.setOnClickListener {
            val randomIntent = Intent(this, MainActivity::class.java)
            startActivity(randomIntent)
        }
        val GoToMedia_player: Button = findViewById(R.id.bGoToMedia_player)
        GoToMedia_player.setOnClickListener {
            val randomIntent = Intent(this, MediaPlayerActivity::class.java)
            startActivity(randomIntent)
        }
    }
    private fun animation_text(){
        val menuText = findViewById<TextView>(R.id.textView)
        val animation = AnimationUtils.loadAnimation(this, R.anim.anim_for_text)
        menuText.startAnimation(animation)
    }
}
