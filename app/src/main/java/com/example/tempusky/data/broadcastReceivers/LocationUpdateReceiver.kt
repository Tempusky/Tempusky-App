package com.example.tempusky.data.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log

class LocationUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent != null && intent.hasExtra("location")) {
            val location = intent.getParcelableExtra<Location>("location")
            Log.d(TAG, "New Location: " + location!!.latitude + ", " + location.longitude)
            // Call method to send location to server
            sendLocationToServer(context, location)
        }
    }

    private fun sendLocationToServer(context: Context, location: Location?) {
        // Send location to server

    }

    companion object {
        private const val TAG = "LocationUpdateReceiver"
    }
}

