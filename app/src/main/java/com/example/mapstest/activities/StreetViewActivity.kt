package com.example.mapstest.activities

import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import com.example.mapstest.R
import com.example.mapstest.databinding.ActivityStreetViewBinding
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.*

class StreetViewActivity : AppCompatActivity(), OnStreetViewPanoramaReadyCallback {
    private val binding: ActivityStreetViewBinding by lazy {
        ActivityStreetViewBinding.inflate(layoutInflater)
    }

    companion object {
        private const val PANORAMA_CAMERA_DURATION = 1000
        private const val TAG = "MainActivity"
        private const val STREET_VIEW_BUNDLE = "StreetViewBundle"
    }

    private lateinit var latLng: LatLng
    private var streetViewPanorama: StreetViewPanorama? = null
    private lateinit var streetViewPanoramaFragment: SupportStreetViewPanoramaFragment

    private var streetViewPanoramaChangeListener =
        StreetViewPanorama.OnStreetViewPanoramaChangeListener { streetViewPanoramaLocation: StreetViewPanoramaLocation? ->
            Log.e(
                TAG, "Street View Panorama Change Listener"
            )
        }

    private var streetViewPanoramaClickListener =
        StreetViewPanorama.OnStreetViewPanoramaClickListener { orientation: StreetViewPanoramaOrientation? ->
            val point: Point? = streetViewPanorama!!.orientationToPoint(
                orientation!!
            )
            if (point != null) {
                streetViewPanorama!!.animateTo(
                    StreetViewPanoramaCamera.Builder()
                        .orientation(orientation)
                        .zoom(streetViewPanorama!!.panoramaCamera.zoom)
                        .build(), PANORAMA_CAMERA_DURATION.toLong()
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val intent = intent
        if (intent != null) {
            val lat = intent.getStringExtra("streetViewLat")
            val lon = intent.getStringExtra("streetViewLon")
            latLng = LatLng(lat!!.toDouble(), lon!!.toDouble())
            Log.d(TAG, "onCreate: location= $latLng")
        }


        streetViewPanoramaFragment =
            supportFragmentManager.findFragmentById(R.id.street_view_panorama) as SupportStreetViewPanoramaFragment
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this)

        var streetViewBundle: Bundle? = null
        if (savedInstanceState != null) streetViewBundle =
            savedInstanceState.getBundle(STREET_VIEW_BUNDLE)
        streetViewPanoramaFragment.onCreate(streetViewBundle)
    }

    override fun onResume() {
        super.onResume()
        streetViewPanoramaFragment.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        var mStreetViewBundle = outState.getBundle(STREET_VIEW_BUNDLE)
        if (mStreetViewBundle == null) {
            mStreetViewBundle = Bundle()
            outState.putBundle(STREET_VIEW_BUNDLE, mStreetViewBundle)
        }
        streetViewPanoramaFragment.onSaveInstanceState(mStreetViewBundle)
    }

    override fun onStreetViewPanoramaReady(streetViewPanorama: StreetViewPanorama) {
        this.streetViewPanorama = streetViewPanorama
        this.streetViewPanorama!!.setPosition(latLng, StreetViewSource.OUTDOOR)
        this.streetViewPanorama!!.setOnStreetViewPanoramaChangeListener(
            streetViewPanoramaChangeListener
        )
        this.streetViewPanorama!!.setOnStreetViewPanoramaClickListener(streetViewPanoramaClickListener)
    }

    override fun onStop() {
        super.onStop()
        streetViewPanoramaFragment.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        streetViewPanoramaFragment.onDestroy()
        streetViewPanorama = null
    }
}