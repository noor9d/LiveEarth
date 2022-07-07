package com.example.mapstest.activities

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.mapstest.LocationTracker
import com.example.mapstest.R
import com.example.mapstest.databinding.ActivityStreetViewLocationBinding
import com.example.mapstest.utils.Permissions
import com.example.mapstest.utils.Utils
import com.example.mapstest.utils.Utils.getAddress
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class StreetViewLocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private val binding: ActivityStreetViewLocationBinding by lazy {
        ActivityStreetViewLocationBinding.inflate(layoutInflater)
    }

    companion object {
        const val TAG = "StreetViewLocationActivity"
        const val REQUEST_CODE_CHECK_SETTINGS = 113
    }

    private lateinit var mMap: GoogleMap
    private lateinit var latLng: LatLng
    private lateinit var address: String
    private lateinit var marker: Marker

    @RequiresApi(Build.VERSION_CODES.N)
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                Log.d(LiveMapActivity.TAG, "Precise location access granted")
                if (!Utils.isGPSEnabled(this)) {
                    Utils.enableLocationSettings(this, REQUEST_CODE_CHECK_SETTINGS)
                }
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                Log.d(LiveMapActivity.TAG, "Only approximate location access granted!")
                if (!Utils.isGPSEnabled(this)) {
                    Utils.enableLocationSettings(this, REQUEST_CODE_CHECK_SETTINGS)
                }
            }
            else -> {
                // No location access granted.
                Log.d(LiveMapActivity.TAG, "permission denied!")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /*LocationTracker.getInstance(this)!!.connectToLocation(object :
            LocationTracker.OnLocationUpdateListener {
            override fun onUpdate(lat: Double, lon: Double) {
                latLng = LatLng(lat, lon)
                address = getAddress(this@StreetViewLocationActivity, latLng).toString()
            }
        })*/

        binding.btnStreetView.setOnClickListener{
            if (latLng != null){
                val intent = Intent(this@StreetViewLocationActivity, StreetViewActivity::class.java)
                intent.putExtra("streetViewLat", latLng.latitude.toString())
                intent.putExtra("streetViewLon", latLng.longitude.toString())
                startActivity(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (Permissions.hasLocationPermission(this))
            mMap.isMyLocationEnabled = true
        else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
//        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        if (!Utils.isGPSEnabled(this)) {
            Utils.enableLocationSettings(this, LiveMapActivity.REQUEST_CODE_CHECK_SETTINGS)
        } /*else addMarker(latLng)*/

        googleMap.setOnMapClickListener { point ->
            latLng = point
            binding.tvAddressValue.text = getAddress(this, latLng)
        }
    }

    private fun addMarker(latLng: LatLng) {
        if (Utils.isGPSEnabled(this)) {
            Log.d(LiveMapActivity.TAG, "onCreate: gps enabled")

            // Add a marker in Sydney and move the camera
            mMap.addMarker(MarkerOptions().position(latLng).title("current location"))!!.showInfoWindow()
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            val loc = CameraUpdateFactory.newLatLngZoom(
                latLng, 16.5f
            )
            mMap.animateCamera(loc)
        } else {
            Log.d(LiveMapActivity.TAG, "onCreate: gps disabled")
            Utils.enableLocationSettings(this, LiveMapActivity.REQUEST_CODE_CHECK_SETTINGS)
        }
    }
}