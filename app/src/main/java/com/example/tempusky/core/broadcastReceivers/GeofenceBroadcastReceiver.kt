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
import com.example.tempusky.core.helpers.GeofencesHelper
import com.example.tempusky.data.GeofenceData
import com.example.tempusky.data.SearchDataResult
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceReceiver"
    }

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Geofence event received")
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
        Log.d(TAG, "Geofence transition")
        for (geofence in triggeringGeofences) {
            val geofenceId = geofence.requestId
            when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    db.collection("geofences").get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                Log.d(TAG, "Entered geofence: $geofenceId")
                                val data = document.data
                                if(data["geofence_id"].toString() == geofenceId){
                                    Log.d(TAG, "Entered geofence: $geofenceId")
                                    db.collection("environment_sensors_data").get()
                                        .addOnSuccessListener { data2 ->
                                            val totalTemp = 0.0
                                            val totalHumidity = 0.0
                                            val totalPressure = 0.0
                                            for (document2 in data2) {
                                                if (document2.data["location"].toString() == data["location"].toString()) {
                                                    val mapData = SearchDataResult(
                                                        document2.data["username"].toString(),
                                                        document2.data["location"].toString(),
                                                        document2.data["temperature"].toString().toDouble(),
                                                        document2.data["humidity"].toString().toDouble(),
                                                        document2.data["pressure"].toString().toDouble(),
                                                        document2.data["timestamp"].toString()
                                                    )
                                                    Log.d(TAG, "Data: $mapData")
                                                    totalTemp.plus(mapData.temperature)
                                                    totalHumidity.plus(mapData.humidity)
                                                    totalPressure.plus(mapData.pressure)
                                                }
                                            }
                                            val notificationText = "Average Temperature: ${totalTemp/data2.size()}\n"
                                            showNotification(context, "You Entered ${data["location"]}", notificationText)
                                        }
                                }
                            }
                        }
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
