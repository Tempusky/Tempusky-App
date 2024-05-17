package com.example.tempusky.core.helpers

import android.Manifest
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.tempusky.MainActivity
import com.example.tempusky.core.broadcastReceivers.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

object GeofencesHelper {

    private const val TAG = "GeofenceManager"
    private const val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = 12 * 60 * 60 * 1000 // 12 hours

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofencePendingIntent: PendingIntent

    fun initialize(context: Context) {
        geofencingClient = LocationServices.getGeofencingClient(context)
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        geofencePendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or FLAG_MUTABLE
        )
    }

    fun addGeofence(
        context: Context,
        geofenceId: String,
        latitude: Double,
        longitude: Double,
        radius: Float
    ) {
        if (!::geofencingClient.isInitialized) {
            initialize(context)
        }

        val geofence = Geofence.Builder()
            .setRequestId(geofenceId)
            .setCircularRegion(latitude, longitude, radius)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            .addGeofence(geofence)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d(TAG, "Geofence added successfully: $geofenceId, $latitude, $longitude, $radius")
            }
            addOnFailureListener {
                Log.e(TAG, "Failed to add geofence: $geofenceId", it)
            }
        }
    }

    fun removeGeofence(geofenceId: String) {
        if (!::geofencingClient.isInitialized) {
            Log.e(TAG, "GeofencingClient is not initialized")
            return
        }

        geofencingClient.removeGeofences(listOf(geofenceId)).run {
            addOnSuccessListener {
                Log.d(TAG, "Geofence removed successfully: $geofenceId")
            }
            addOnFailureListener {
                Log.e(TAG, "Failed to remove geofence: $geofenceId", it)
            }
        }
    }
}