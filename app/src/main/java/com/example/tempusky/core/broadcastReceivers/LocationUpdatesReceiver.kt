package com.example.tempusky.core.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.tempusky.core.services.EnvironmentSensorsService
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult

class LocationUpdatesReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive() context:$context, intent:$intent")

        if (intent.action == ACTION_PROCESS_UPDATES) {

            // Checks for location availability changes.
            LocationAvailability.extractLocationAvailability(intent).let { locationAvailability ->
                if (!locationAvailability?.isLocationAvailable!!) {
                    Log.d(TAG, "Location services are no longer available!")
                }
            }
            var latitude = 0.0
            var longitude = 0.0
            LocationResult.extractResult(intent).let { locationResult ->
                val locations = locationResult?.locations?.map { location ->
                    latitude = location.latitude
                    longitude = location.longitude
                }
                if (locations != null) {
                    if (locations.isNotEmpty()) {
                        Log.d(TAG, "Location Data: $latitude, $longitude")
                        // Call method to send location data to server
                        startEnvironmentSensorsService(context, latitude, longitude)
                    }
                }
            }
        }
    }

    private fun startEnvironmentSensorsService(context: Context, latitude: Double, longitude: Double) {
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