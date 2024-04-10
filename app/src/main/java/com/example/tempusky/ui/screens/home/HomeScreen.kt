package com.example.tempusky.ui.screens.home

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.tempusky.MainActivity
import com.example.tempusky.MainViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.DefaultSettingsProvider
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck

@OptIn(MapboxExperimental::class)
@Composable
fun HomeScreen(context: MainActivity, mainViewModel: MainViewModel) {
    MainActivity.locationPermissionLauncher.launch(
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val darkThemeMap = "mapbox://styles/faysalbadaoui/cluikavl200jr01r2hsgu2ejc"
    val lightThemeMap = "mapbox://styles/mapbox/outdoors-v12"
    var deviceTheme by remember { mutableStateOf("") }
    mainViewModel.appTheme.observe(context) {
        deviceTheme = it
    }
    LaunchedEffect(Unit){
        Log.d("HomeScreen", "Saved theme:${deviceTheme}")
    }
    key(deviceTheme){
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapInitOptionsFactory = { context ->
                MapInitOptions(
                    context = context,
                    styleUri = if(deviceTheme == "Dark") darkThemeMap else lightThemeMap,
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
            locationComponentSettings = DefaultSettingsProvider.defaultLocationComponentSettings(
                context = LocalContext.current
            ).toBuilder()
                .setLocationPuck(createDefault2DPuck(withBearing = false))
                .setEnabled(true)
                .build()
        )
    }

}