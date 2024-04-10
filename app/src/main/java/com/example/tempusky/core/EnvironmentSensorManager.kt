package com.example.tempusky.core

import android.content.Context
import android.content.Intent
import com.example.tempusky.core.broadcastReceivers.EnvironmentSensorsReceiver

class EnvironmentSensorManager(private val context: Context) {

    fun updateValues() {
        val intent = Intent(context, EnvironmentSensorsReceiver::class.java)
        context.sendBroadcast(intent)
    }

    companion object {
        private const val TAG = "EnvironmentSensorManager"
    }
}
