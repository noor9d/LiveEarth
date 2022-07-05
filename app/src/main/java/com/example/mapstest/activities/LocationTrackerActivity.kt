package com.example.mapstest.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mapstest.R
import com.example.mapstest.databinding.ActivityLocationTrackerBinding
import com.example.mapstest.models.LocationModel
import com.example.mapstest.services.LocationService
import com.example.mapstest.utils.Constants
import com.example.mapstest.utils.UtilsFunction.getBitmapFromVectorDrawable
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.utsman.smartmarker.moveMarkerSmoothly

class LocationTrackerActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityLocationTrackerBinding
    private lateinit var mMap: GoogleMap
    private lateinit var mRefLocation: DatabaseReference
    private lateinit var listenerLocation: ValueEventListener
    private var userMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initClick()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        retrieveLocation()
    }

    private fun initClick() {
        with(binding) {
            mbStart.setOnClickListener {
                Intent(
                    this@LocationTrackerActivity,
                    LocationService::class.java
                ).also { service ->
                    service.action = LocationService.ACTION_START_FOREGROUND_SERVICE
                    tvServiceInfo.text = getString(R.string.service_running)
                    startService(service)
                }
            }

            mbStop.setOnClickListener {
                Intent(this@LocationTrackerActivity, LocationService::class.java).also {
                    tvServiceInfo.text = getString(R.string.service_stopped)
                    stopService(it)
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun retrieveLocation() {
        mRefLocation = FirebaseDatabase.getInstance().getReference(Constants.LOCATION_TRACKER).child(
                Constants.LOCATION
            ).child("12345")

        mRefLocation.keepSynced(true)

        mRefLocation.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val location = snapshot.getValue(LocationModel::class.java)
                Log.d("loc", "onDataChange: ${location.toString()}")
                location?.run {
                    userMarker = mMap.addMarker(
                        MarkerOptions().position(LatLng(location.latitude, location.longitude))
                            .title("Marker")
                            .anchor(0.5.toFloat(), 0.5.toFloat())
                            .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(R.drawable.ic_marker)))
                    )

                    binding.tvServiceInfo.text = location.speed.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })

        listenerLocation = mRefLocation.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val location = snapshot.getValue(LocationModel::class.java)
                location?.run {
                    userMarker?.moveMarkerSmoothly(LatLng(location.latitude, location.longitude), true)
                    updateCameraMap(LatLng(this.latitude, this.longitude))
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateCameraMap(point1: LatLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point1, 16.0f))
    }

    override fun onDestroy() {
        mRefLocation.removeEventListener(listenerLocation)
        super.onDestroy()
    }
}