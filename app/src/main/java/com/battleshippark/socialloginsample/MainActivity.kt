package com.battleshippark.socialloginsample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.battleshippark.socialloginsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.facebook.setOnClickListener {
            startActivity(Intent(this, FacebookActivity::class.java))
        }

        binding.google.setOnClickListener {
            startActivity(Intent(this, GoogleActivity::class.java))
        }
    }
}
