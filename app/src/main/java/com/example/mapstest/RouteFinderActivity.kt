package com.example.mapstest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mapstest.LocationTracker.Companion.instance
import com.example.mapstest.LocationTracker.OnLocationUpdateListener
import com.example.mapstest.databinding.ActivityRouteFinderBinding


class RouteFinderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRouteFinderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteFinderBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}