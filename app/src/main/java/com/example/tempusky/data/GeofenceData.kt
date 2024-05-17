package com.example.tempusky.data

import com.google.firebase.firestore.GeoPoint

data class GeofenceData(
    val id: String,
    val location: String,
    val location_coords: GeoPoint,
    val radius: Float
)