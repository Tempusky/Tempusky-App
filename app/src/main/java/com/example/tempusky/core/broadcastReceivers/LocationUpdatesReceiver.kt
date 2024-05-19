package com.example.tempusky.core.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.example.tempusky.core.services.EnvironmentSensorsService
import com.google.android.gms.location.LocationResult

class LocationUpdatesReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive() context:$context, intent:$intent")

        if (intent.action == ACTION_PROCESS_UPDATES) {
            //Treat the case if is button, else its the location result
            val location = intent.getParcelableExtra<Location>("location")
            location?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                Log.d(TAG, "Location Data: $latitude, $longitude")
                startEnvironmentSensorsService(context, latitude, longitude)
            } ?: run {
                LocationResult.extractResult(intent)?.let { locationResult ->
                    locationResult.locations.firstOrNull()?.let { loc ->
                        val latitude = loc.latitude
                        val longitude = loc.longitude
                        Log.d(TAG, "Location Data from LocationResult: $latitude, $longitude")
                        startEnvironmentSensorsService(context, latitude, longitude)
                    }
                }
            }
        }
    }

    private fun startEnvironmentSensorsService(
        context: Context,
        latitude: Double,
        longitude: Double
    ) {
        // START ENVIRONMENT SENSORS SERVICE
        val intent = Intent(context, EnvironmentSensorsService::class.java)
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        context.startForegroundService(intent)
    }

    companion object {
        private const val TAG = "LocationDataReceiver"
        const val ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.example.tempusky.action." +
                    "PROCESS_UPDATES"
    }
}