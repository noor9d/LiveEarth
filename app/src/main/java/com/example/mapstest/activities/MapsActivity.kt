package com.example.mapstest.activities

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.mapstest.LocationTracker
import com.example.mapstest.R
import com.example.mapstest.utils.Utils
import com.example.mapstest.databinding.ActivityMapsBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        const val TAG = "MapsActivityTAG"
        const val REQUEST_CODE_CHECK_SETTINGS = 111
    }

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private lateinit var latLng: LatLng

    @RequiresApi(Build.VERSION_CODES.N)
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                Log.d(TAG, "Precise location access granted")
                if (!Utils.isGPSEnabled(this)) {
                    Utils.enableLocationSettings(this)
                }
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                Log.d(TAG, "Only approximate location access granted!")
                if (!Utils.isGPSEnabled(this)) {
                    Utils.enableLocationSettings(this)
                }
            }
            else -> {
                // No location access granted.
                Log.d(TAG, "permission denied!")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        LocationTracker.getInstance(this)!!.connectToLocation(object :
            LocationTracker.OnLocationUpdateListener {
            override fun onUpdate(lat: Double, lon: Double) {
                Log.d(TAG, "onUpdate: lat: $lat ---- lon: $lon")
                latLng = LatLng(lat, lon)
                getAddress(latLng)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // When map is initially loaded, determine which map type option to 'select'
        when (mMap.mapType) {
            GoogleMap.MAP_TYPE_SATELLITE -> {
                binding.mapTypeSatelliteBackground.visibility = View.VISIBLE
                binding.mapTypeSatelliteText.setTextColor(Color.BLUE)
            }
            GoogleMap.MAP_TYPE_TERRAIN -> {
                binding.mapTypeTerrainBackground.visibility = View.VISIBLE
                binding.mapTypeTerrainText.setTextColor(Color.BLUE)
            }
            else -> {
                binding.mapTypeDefaultBackground.visibility = View.VISIBLE
                binding.mapTypeDefaultText.setTextColor(Color.BLUE)
            }
        }

        // Set click listener on FAB to open the map type selection view
        binding.mapTypeFAB.setOnClickListener {

            // Start animator to reveal the selection view, starting from the FAB itself
            val anim = ViewAnimationUtils.createCircularReveal(
                binding.mapTypeSelection,
                binding.mapTypeSelection.width - (binding.mapTypeFAB.width / 2),
                binding.mapTypeSelection.height / 2,
                binding.mapTypeFAB.width / 2f,
                binding.mapTypeSelection.width.toFloat())
            anim.duration = 200
            anim.interpolator = AccelerateDecelerateInterpolator()

            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    binding.mapTypeSelection.visibility = View.VISIBLE
                }
            })

            anim.start()
            binding.mapTypeFAB.visibility = View.INVISIBLE

        }

        // Set click listener on the map to close the map type selection view
        mMap.setOnMapClickListener {

            // Conduct the animation if the FAB is invisible (window open)
            if (binding.mapTypeFAB.visibility == View.INVISIBLE) {

                // Start animator close and finish at the FAB position
                val anim = ViewAnimationUtils.createCircularReveal(
                    binding.mapTypeSelection,
                    binding.mapTypeSelection.width - (binding.mapTypeFAB.width / 2),
                    binding.mapTypeFAB.height / 2,
                    binding.mapTypeSelection.width.toFloat(),
                    binding.mapTypeFAB.width / 2f)
                anim.duration = 200
                anim.interpolator = AccelerateDecelerateInterpolator()

                anim.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        binding.mapTypeSelection.visibility = View.INVISIBLE
                    }
                })

                // Set a delay to reveal the FAB. Looks better than revealing at end of animation
                Handler(Looper.getMainLooper()).postDelayed({
                    kotlin.run {
                        binding.mapTypeFAB.visibility = View.VISIBLE
                    }
                }, 100)
                anim.start()
            }
        }

        // Handle selection of the Default map type
        binding.mapTypeDefault.setOnClickListener {
            binding.mapTypeDefaultBackground.visibility = View.VISIBLE
            binding.mapTypeSatelliteBackground.visibility = View.INVISIBLE
            binding.mapTypeTerrainBackground.visibility = View.INVISIBLE
            binding.mapTypeDefaultText.setTextColor(Color.parseColor("#2196F3"))
            binding.mapTypeSatelliteText.setTextColor(Color.parseColor("#808080"))
            binding.mapTypeTerrainText.setTextColor(Color.parseColor("#808080"))
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        // Handle selection of the Satellite map type
        binding.mapTypeSatellite.setOnClickListener {
            binding.mapTypeDefaultBackground.visibility = View.INVISIBLE
            binding.mapTypeSatelliteBackground.visibility = View.VISIBLE
            binding.mapTypeTerrainBackground.visibility = View.INVISIBLE
            binding.mapTypeDefaultText.setTextColor(Color.parseColor("#808080"))
            binding.mapTypeSatelliteText.setTextColor(Color.BLUE)
            binding.mapTypeTerrainText.setTextColor(Color.parseColor("#808080"))
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }

        // Handle selection of the terrain map type
        binding.mapTypeTerrain.setOnClickListener {
            binding.mapTypeDefaultBackground.visibility = View.INVISIBLE
            binding.mapTypeSatelliteBackground.visibility = View.INVISIBLE
            binding.mapTypeTerrainBackground.visibility = View.VISIBLE
            binding.mapTypeDefaultText.setTextColor(Color.parseColor("#808080"))
            binding.mapTypeSatelliteText.setTextColor(Color.parseColor("#808080"))
            binding.mapTypeTerrainText.setTextColor(Color.BLUE)
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }

        if (Utils.hasLocationPermission(this))
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
            Utils.enableLocationSettings(this)
        } else addMarker()
    }

    private fun addMarker() {
        if (Utils.isGPSEnabled(this)) {
            Log.d(TAG, "onCreate: gps enabled")
//            getAddress(latLng)

            // Add a marker in Sydney and move the camera
            mMap.addMarker(MarkerOptions().position(latLng).title("current location"))!!.showInfoWindow()
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            val loc = CameraUpdateFactory.newLatLngZoom(
                latLng, 16.5f
            )
            mMap.animateCamera(loc)
        } else {
            Log.d(TAG, "onCreate: gps disabled")
            Utils.enableLocationSettings(this)
        }
    }

    private fun getAddress(latLng: LatLng) {
        try {
            val addresses: List<Address>
            val geocoder = Geocoder(this, Locale.getDefault())

            addresses = geocoder.getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1
            ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


            val address: String =
                addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            val city: String = addresses[0].locality
            val state: String = addresses[0].adminArea
            val country: String = addresses[0].countryName
            val postalCode: String = addresses[0].postalCode
            val knownName: String =
                addresses[0].featureName // Only if available else return NULL

            Log.d(TAG, "getAddress: \n address= $address\n city= $city \n state= $state \ncountry= $country\n postal code= $postalCode\n knownName= $knownName")

        } catch (e: Exception) {
            Log.d(TAG, "getAddress: ${e.printStackTrace()}")
        }
    }

    /*fun onMapSearch(view: View?) {
        val location = binding.editText.text.toString()
        var addressList: List<Address>? = null
        if (location.isNotEmpty()) {
            val geocoder = Geocoder(this)
            try {
                addressList = geocoder.getFromLocationName(location, 1)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val address = addressList!![0]
            val latLng = LatLng(address.latitude, address.longitude)
            mMap.addMarker(MarkerOptions().position(latLng).title("Marker"))
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    fun onNormalMap(view: View?) {
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    fun onSatelliteMap(view: View?) {
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
    }

    fun onTerrainMap(view: View?) {
        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
    }

    fun onHybridMap(view: View?) {
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
    }*/
}
