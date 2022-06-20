package com.example.mapstest

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.mapstest.databinding.ActivityMainBinding
import com.example.mapstest.databinding.ActivityMapsBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.N)
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                Log.d(MapsActivity.TAG, "Precise location access granted")
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                Log.d(MapsActivity.TAG, "Only approximate location access granted!")
            }
            else -> {
                // No location access granted.
                Log.d(MapsActivity.TAG, "permission denied!")
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
            startActivity(Intent(this@MainActivity, MapsActivity::class.java))
        }

        binding.btnLiveTraffic.setOnClickListener {
            startActivity(Intent(this@MainActivity, LiveTrafficActivity::class.java))
        }

        binding.btnRouteFinder.setOnClickListener {
            startActivity(Intent(this@MainActivity, RouteFinderActivity::class.java))
        }
    }
}