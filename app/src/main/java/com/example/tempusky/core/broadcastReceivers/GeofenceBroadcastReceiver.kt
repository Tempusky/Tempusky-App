package com.example.tempusky.core.broadcastReceivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.tempusky.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent!!.hasError()) {
            val errorMessage = geofencingEvent.errorCode.toString()
            Log.e(TAG, "Geofencing event error: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            val triggeringGeofences = geofencingEvent.triggeringGeofences
            handleGeofenceTransition(context, geofenceTransition, triggeringGeofences!!)
        } else {
            Log.e(TAG, "Invalid geofence transition type: $geofenceTransition")
        }
    }

    private fun handleGeofenceTransition(
        context: Context,
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ) {
        for (geofence in triggeringGeofences) {
            val geofenceId = geofence.requestId

            when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    Log.d(TAG, "Entered geofence: $geofenceId")
                    showNotification(context, "Geofence Entered", "You have entered the geofence: $geofenceId")
                }
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    Log.d(TAG, "Exited geofence: $geofenceId")
                    showNotification(context, "Geofence Exited", "You have exited the geofence: $geofenceId")
                }
                Geofence.GEOFENCE_TRANSITION_DWELL -> {
                    Log.d(TAG, "Dwelling in geofence: $geofenceId")
                    showNotification(context, "Geofence Dwelling", "You are dwelling in the geofence: $geofenceId")
                }
            }
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        val notificationId = 1
        val channelId = "geofence_channel"

        // Create notification channel if necessary (for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Geofence Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for geofence transitions"
            }
            notificationManager?.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager?.notify(notificationId, notificationBuilder.build())
    }

}
