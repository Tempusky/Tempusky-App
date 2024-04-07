package com.example.tempusky.core.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.tempusky.MainActivity
import com.example.tempusky.MainActivity.Companion.context
import com.example.tempusky.MainActivity.Companion.mFusedLocationClient
import com.example.tempusky.MainActivity.Companion.mSettingsClient
import com.example.tempusky.core.broadcastReceivers.LocationUpdateReceiver
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority


class LocationForegroundService : Service() {

    private lateinit var mLocationRequest: LocationRequest
    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest

    /**
     * Callback for Location events.
     */
    private lateinit var mLocationCallback: LocationCallback
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "start location service as foreground")
        startForegroundService()
        createLocationCallback()
        createLocationRequest()
        buildLocationSettingsRequest()
        startLocationUpdates()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        Log.d(TAG, "start foreground service")
        val CHANNEL_ID = "01"
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Accessing Location Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Accessing Location Service")
            .setContentText("The app is accessing the location service to retrieve the location").build()
        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    private fun createLocationCallback() {
        Log.d(TAG, "create location callback")
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                MainActivity.locationViewModel.setLatitude(locationResult.lastLocation!!.latitude)
                MainActivity.locationViewModel.setLongitude(locationResult.lastLocation!!.longitude)
                super.onLocationResult(locationResult)
                val intent = Intent(
                    context,
                    LocationUpdateReceiver::class.java
                )
                intent.putExtra("location", locationResult.lastLocation)
                context.sendBroadcast(intent)
            }
        }
    }
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.Builder(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
            .setMaxUpdateDelayMillis(UPDATE_INTERVAL_IN_MILLISECONDS)
            .build()


        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.


        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
    }
    /**
     * Uses a [com.google.android.gms.location.LocationSettingsRequest.Builder] to build
     * a [com.google.android.gms.location.LocationSettingsRequest] that is used for checking
     * if a device has the needed location settings.
     */
    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()
    }

    private fun startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        MainActivity.mRequestingLocationUpdates = true
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(context) {
                Log.i(TAG, "All location settings are satisfied.")
                if ((checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED
                            ) || (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, 0, 0) ==
                            PackageManager.PERMISSION_GRANTED
                            )) mFusedLocationClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback, Looper.myLooper()!!
                )

            }
            .addOnFailureListener(context) { e ->
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.i(
                            TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings "
                        )
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(context,
                                REQUEST_CHECK_SETTINGS
                            )
                        } catch (sie: IntentSender.SendIntentException) {
                            Log.i(TAG, "PendingIntent unable to execute request.")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings."
                        Log.e(TAG, errorMessage)
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }

                    else -> {}
                }
            }
    }

    companion object {
        private val TAG = LocationForegroundService::class.java.simpleName

        /**
         * Constant used in the location settings dialog.
         */
        private const val REQUEST_CHECK_SETTINGS = 0x1

        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 100  // 10000

        /**
         * The fastest rate for active location updates. Exact. Updates will never be more frequent
         * than this value.
         */
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2

        // Keys for storing activity state in the Bundle.
        private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates"
        private const val KEY_LOCATION = "location"
        private const val KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string"

    }
}