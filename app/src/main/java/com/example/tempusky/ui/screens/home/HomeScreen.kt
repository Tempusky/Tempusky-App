package com.example.tempusky.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.style.style

@OptIn(MapboxExperimental::class)
@Composable
fun HomeScreen() {
    val darkThemeMap = "mapbox://styles/faysalbadaoui/cluikavl200jr01r2hsgu2ejc"
    val lightThemeMap = "mapbox://styles/mapbox/outdoors-v12"
    MapboxMap(
        Modifier.fillMaxSize(),
        mapInitOptionsFactory = { context ->
            MapInitOptions(
                context = context,
                styleUri = lightThemeMap,
            )
        },
        mapViewportState = MapViewportState().apply {
            setCameraOptions {
                zoom(11.0)
                center(Point.fromLngLat(0.62, 41.6167))
                pitch(0.0)
                bearing(0.0)
            }
            style{
                Style.DARK
            }
        },

    )

}