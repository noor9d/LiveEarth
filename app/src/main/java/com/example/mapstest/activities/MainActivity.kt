package com.example.mapstest.activities

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.mapstest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.N)
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                Log.d(LiveMapActivity.TAG, "Precise location access granted")
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                Log.d(LiveMapActivity.TAG, "Only approximate location access granted!")
            }
            else -> {
                // No location access granted.
                Log.d(LiveMapActivity.TAG, "permission denied!")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLiveMap.setOnClickListener {
            startActivity(Intent(this@MainActivity, LiveMapActivity::class.java))
        }

        binding.btnLiveTraffic.setOnClickListener {
            startActivity(Intent(this@MainActivity, LiveTrafficActivity::class.java))
        }

        binding.btnStreetView.setOnClickListener {
            startActivity(Intent(this@MainActivity, StreetViewLocationActivity::class.java))
        }

        binding.btnLiveCameras.setOnClickListener {
            startActivity(Intent(this@MainActivity, LiveCamerasActivity::class.java))
        }

        binding.btnLocationTracker.setOnClickListener {
            startActivity(Intent(this@MainActivity, LocationTrackerActivity::class.java))
        }
    }
}