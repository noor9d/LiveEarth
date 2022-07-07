package com.example.mapstest.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mapstest.R
import com.example.mapstest.databinding.ActivityLocationTrackerBinding
import com.example.mapstest.models.LocationModel
import com.example.mapstest.services.LocationService
import com.example.mapstest.utils.Constants
import com.example.mapstest.utils.UtilsFunction.getBitmapFromVectorDrawable
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.utsman.smartmarker.moveMarkerSmoothly
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


class LocationTrackerActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityLocationTrackerBinding
    private var mRefLocation: DatabaseReference? = null
    private lateinit var listenerLocation: ValueEventListener
    private var userMarker: Marker? = null
    private var serviceRunning: Boolean = false
    private var lastKnownLatLng: LatLng? = null
    private var currentMarker: Marker? = null
    private var initialMarker: Marker? = null
    private var callback: SnapshotReadyCallback? = null

    companion object {
        const val TAG = "LocationTrackerActivity"
        private const val COLOR_BLACK_ARGB = -0x1000000
        private const val POLYLINE_STROKE_WIDTH_PX = 15
        var points: ArrayList<LatLng> = ArrayList()
        private lateinit var mMap: GoogleMap
        private var polylineOptions: PolylineOptions? = null
        private var polyline: Polyline? = null

        fun updateTrack() {
            Log.d(TAG, "updateTrack: called!")
            try {
                polylineOptions = PolylineOptions()
                polylineOptions!!.addAll(points)
                polylineOptions!!.width(POLYLINE_STROKE_WIDTH_PX.toFloat())
                polylineOptions!!.geodesic(true)

                polyline = mMap.addPolyline(polylineOptions!!)
                polyline!!.color = COLOR_BLACK_ARGB
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initClick()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initClick() {
        with(binding) {
            btnStartStop.setOnClickListener {
                if (!serviceRunning) {
                    Intent(
                        this@LocationTrackerActivity,
                        LocationService::class.java
                    ).also { service ->
                        service.action = LocationService.ACTION_START_FOREGROUND_SERVICE
                        serviceRunning = true
                        binding.btnStartStop.text = getString(R.string.stop_text)
                        retrieveLocation()
                        Toast.makeText(
                            this@LocationTrackerActivity,
                            getString(R.string.service_running),
                            Toast.LENGTH_SHORT
                        ).show()
                        startService(service)
                    }
                } else {
                    Intent(this@LocationTrackerActivity, LocationService::class.java).also {
                        serviceRunning = false
                        binding.btnStartStop.text = getString(R.string.start_text)
                        Toast.makeText(
                            this@LocationTrackerActivity,
                            getString(R.string.service_stopped),
                            Toast.LENGTH_SHORT
                        ).show()
                        clearTextViews()
                        showSaveDialog(this@LocationTrackerActivity)
                        stopService(it)
                    }
                }
            }
        }
    }

    private fun showSaveDialog(activity: Activity) {
        Log.d(TAG, "showSaveDialog: show dialog")
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.save_track_dailog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val save = dialog.findViewById<Button>(R.id.save)
        val tripNameEd = dialog.findViewById<EditText>(R.id.trip_name_ed)
        val localCalendar = Calendar.getInstance(TimeZone.getDefault())
        val currentTime = localCalendar.time
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss aaa")
        val timestamp = df.format(currentTime)
        tripNameEd.setText(timestamp)

        // save button
        save.setOnClickListener {
            Dexter.withContext(activity)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        val time: String = binding.tvDurationValue.text.toString()
                        val distance: String = binding.tvDistanceValue.text.toString()
                        Log.d("Timestamp", timestamp)
                        try {
                            snapShot(timestamp, activity)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                        clearTextViews()
                        dialog.cancel()
                        dialog.dismiss()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (response.isPermanentlyDenied) {
                            AlertDialog.Builder(activity, R.style.DialogeTheme)
                                .setTitle(R.string.allow_permission_text)
                                .setMessage(R.string.permanently_denied_message)
                                .setNegativeButton(activity.resources.getString(R.string.cancel),
                                    DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                                .setPositiveButton(activity.resources.getString(R.string.settings),
                                    DialogInterface.OnClickListener { dialog, which ->
                                        val intent = Intent()
                                        intent.action =
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                        val uri = Uri.fromParts(
                                            "package",
                                            activity.packageName,
                                            null
                                        )
                                        intent.data = uri
                                        activity.startActivity(intent)
                                    })
                                .show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
        }


        val cancel = dialog.findViewById<Button>(R.id.cancel)
        cancel.setOnClickListener {
            try {
                mMap.clear()
                points.clear()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            clearTextViews()
            dialog.dismiss()
        }


        try {
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun snapShot(
        timeStamp: String,
        context: Context
    ) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("ddMMMyyyyhhmmss")
        val datetime = dateFormat.format(calendar.time)
        callback = SnapshotReadyCallback { snapshot ->
                val executor = Executors.newSingleThreadExecutor()
                val handler = Handler(Looper.getMainLooper())
                executor.execute {
                    //Background work here
                    saveToSdCard(snapshot, context, timeStamp)
                    handler.post {
                        //UI Thread work here
                        mMap.clear()
                        points.clear()
                        Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
                        clearTextViews()
                    }
                }
            }
        mMap.snapshot(callback!!)
    }

    private fun saveToSdCard(snapshot: Bitmap?, context: Context, timeStamp: String) {
        val sdcard = getExternalFilesDir("/Tracks")
        var mediaDir: File? = null
        // for avoiding crash if device doesn't has this directory
        if (sdcard != null) {
            mediaDir = File(sdcard.absolutePath)
            if (!mediaDir.exists()) {
                mediaDir.mkdirs()
            }
        }

        val dest = File(mediaDir, "$timeStamp.png")
        val outputStream = ByteArrayOutputStream()
        snapshot!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(dest)
        } catch (e1: FileNotFoundException) {
            e1.printStackTrace()
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.write(outputStream.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                fileOutputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                fileOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return strAdd
    }

    private fun clearTextViews() {
        binding.tvSpeedValue.text = "00.00"
        binding.tvDurationValue.text = "00.00"
        binding.tvDistanceValue.text = "00.00"
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        /*try {
            if (currentMarker != null) currentMarker!!.remove()
            val latLng = LatLng(
                points[points.size - 1].latitude,
                points[points.size - 1].longitude
            )

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            points[points.size - 1].latitude,
                                points[points.size - 1].longitude), 12.0f));

            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, 18.0f)
            )
            currentMarker = googleMap.addMarker(
                MarkerOptions().position(
                    LatLng(
                        points[points.size - 1].latitude,
                        points[points.size - 1].longitude
                    )
                )
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            initialMarker = googleMap.addMarker(
                MarkerOptions().position(
                    LatLng(
                        points[0].latitude,
                        points[0].longitude
                    )
                )
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
            polyline!!.points = points
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }*/
    }

    private fun retrieveLocation() {
        mRefLocation =
            FirebaseDatabase.getInstance().getReference(Constants.LOCATION_TRACKER).child(
                Constants.LOCATION
            ).child("12345")

        mRefLocation!!.keepSynced(true)

        mRefLocation!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val location = snapshot.getValue(LocationModel::class.java)
                location?.run {
                    userMarker = mMap.addMarker(
                        MarkerOptions().position(LatLng(location.latitude, location.longitude))
                            .title("Marker")
                            .anchor(0.5.toFloat(), 0.5.toFloat())
                            .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(R.drawable.ic_marker)))
                    )

                    // adding path points
//                    points.add(LatLng(location.latitude, location.longitude))

                    Log.d(TAG, "onDataChange: location= $location")
                    Log.d(TAG, "onDataChange: points = $points")

//                    lastKnownLatLng = LatLng(location.latitude, location.longitude)

                    binding.tvSpeedValue.text =  "${location.speed} m/s"
                    binding.tvDurationValue.text = formatTime(location.time)
                    binding.tvAddressValue.text = getCompleteAddressString(location.latitude, location.longitude)

                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })

        listenerLocation = mRefLocation!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val location = snapshot.getValue(LocationModel::class.java)
                location?.run {
                    userMarker?.moveMarkerSmoothly(
                        LatLng(location.latitude, location.longitude),
                        true
                    )
                    updateCameraMap(LatLng(this.latitude, this.longitude))
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun formatTime(milliSeconds: Long): String {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat("HH:mm:ss")

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    private fun updateCameraMap(point1: LatLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point1, 18.0f))
    }

    override fun onDestroy() {
        if (mRefLocation != null) mRefLocation!!.removeEventListener(listenerLocation)
        super.onDestroy()
    }
}