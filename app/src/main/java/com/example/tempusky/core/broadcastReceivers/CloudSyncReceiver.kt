package com.example.tempusky.core.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CloudSyncReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "CloudSyncReceiver onReceive")
        // Call method to sync data with cloud
        syncDataWithCloud(intent)
    }

    private fun syncDataWithCloud(intent: Intent) {
        // Sync data with cloud
        val temp = intent.getFloatExtra("temperature", 0.0f)
        val pressure = intent.getFloatExtra("pressure", 0.0f)
        val humidity = intent.getFloatExtra("humidity", 0.0f)
        val location = intent.getStringExtra("location")

        Log.d(TAG, "Syncing Data with Cloud: $temp $pressure $humidity")
    }

    companion object {
        private const val TAG = "CloudSyncReceiver"
    }
}