package com.example.tempusky.domain.map

import com.mapbox.geojson.Point

data class MapPointData(
    val id: String,
    val point: Point,
    val title: String,
    val description: String
)
