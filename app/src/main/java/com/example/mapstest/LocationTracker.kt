package com.example.mapstest

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log


class LocationTracker {
    private var isGPSEnabled = false
    private var isNetworkEnabled = false
    private var canGetLocation = false
    private var mLocation: Location? = null
    private var locationManager: LocationManager? = null
    private var listener: OnLocationUpdateListener? = null

    fun connectToLocation(listener: OnLocationUpdateListener?) {
        this.listener = listener
        stopLocationUpdates()
        displayLocation()
    }

    private fun displayLocation() {
        try {
            val location = getLocation()
            if (location != null) {
                updateLatitudeLongitude(location.latitude, location.longitude)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun getLocation(): Location? {
        try {
            locationManager = mContext?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGPSEnabled && !isNetworkEnabled) {
                canGetLocation = false
            } else {
                canGetLocation = true
                if (isNetworkEnabled) {
                    if (locationManager != null) {
                        mLocation = locationManager!!
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        locationManager!!.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), locationProviderListener
                        )
                        return mLocation
                    }
                } else if (isGPSEnabled) {
                    if (locationManager != null) {
                        mLocation = locationManager!!
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), locationProviderListener
                        )
                        return mLocation
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        return mLocation
    }

    fun updateLatitudeLongitude(latitude: Double, longitude: Double) {
        Log.i(TAG, "updated Lat == $latitude  updated long == $longitude")
        listener!!.onUpdate(latitude, longitude)
    }

    private fun stopLocationUpdates() {
        try {
            if (locationManager != null) {
                locationManager!!.removeUpdates(locationProviderListener)
                locationManager = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var locationProviderListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            try {
                val latitude = location.latitude
                val longitude = location.longitude
                updateLatitudeLongitude(latitude, longitude)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
        override fun onProviderEnabled(s: String) {}
        override fun onProviderDisabled(s: String) {}
    }

    interface OnLocationUpdateListener {
        fun onUpdate(lat: Double, lon: Double)
    }

    companion object {
        var TAG: String = LocationTracker::class.java.name
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 0
        private const val MIN_TIME_BW_UPDATES: Long = 1000
        var mContext: Context? = null
        var instance: LocationTracker? = null

        @Synchronized
        fun getInstance(ctx: Context): LocationTracker? {
            mContext = ctx
            if (instance == null) {
                instance = LocationTracker()
            }
            return instance
        }
    }
}