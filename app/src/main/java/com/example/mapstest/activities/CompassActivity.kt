package com.example.mapstest.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mapstest.R
import com.example.mapstest.databinding.ActivityCompassBinding

class CompassActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompassBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCompassBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}