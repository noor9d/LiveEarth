package com.example.mapstest.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mapstest.databinding.ActivityRouteFinderBinding


class RouteFinderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRouteFinderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteFinderBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}