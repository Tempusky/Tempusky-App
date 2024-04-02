package com.example.tempusky.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.tempusky.data.SettingsDataStore
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
    val dataStore = SettingsDataStore(LocalContext.current)
    val savedTheme = dataStore.getTheme.collectAsState(initial = "Light")

    MapboxMap(
        Modifier.fillMaxSize(),
        mapInitOptionsFactory = { context ->
            MapInitOptions(
                context = context,
                styleUri = if(savedTheme.value == "Dark") darkThemeMap else lightThemeMap,
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