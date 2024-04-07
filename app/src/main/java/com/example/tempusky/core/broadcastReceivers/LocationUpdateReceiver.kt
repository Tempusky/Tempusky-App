package com.example.tempusky.core.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.example.tempusky.core.helpers.SensorsDataHelper

class LocationUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "LocationUpdateReceiver onReceive")
        if (intent != null && intent.hasExtra("location")) {
            val location = intent.getParcelableExtra<Location>("location")
            Log.d(TAG, "New Location: " + location!!.latitude + ", " + location.longitude)
            // Call method to send location to server
            sendLocationToServer(context, location)
        }
    }

    private fun sendLocationToServer(context: Context, location: Location?) {
        // Send location to server
        val temp = SensorsDataHelper.tempData.value
        val pressure = SensorsDataHelper.pressureData.value
        val humidity = SensorsDataHelper.humidityData.value

        Log.d(TAG, "Sending Data to API: $temp $pressure $humidity ${location!!.latitude}, ${location.longitude}"  )

    }

    companion object {
        private const val TAG = "LocationUpdateReceiver"
    }
}

