package com.example.tempusky.ui.screens.home

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.tempusky.domain.map.MapPointData
import com.mapbox.geojson.Point
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.DefaultSettingsProvider
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions

@OptIn(MapboxExperimental::class)
@Composable
fun HomeScreen(context: MainActivity, mainViewModel: MainViewModel) {
    val darkThemeMap = "mapbox://styles/faysalbadaoui/cluikavl200jr01r2hsgu2ejc"
    val lightThemeMap = "mapbox://styles/mapbox/outdoors-v12"
    var deviceTheme by remember { mutableStateOf("") }
    var requestingPermissions by remember { mutableStateOf(false) }
    val locations = listOf(
        MapPointData("24.5", Point.fromLngLat(0.6206, 41.6148), "Lleida", "1221 Values available"),
        MapPointData("23", Point.fromLngLat(0.5730, 41.7296), "Albesa", "223 Values available"),
        MapPointData("22.3", Point.fromLngLat(0.8114, 41.7910), "Balaguer", "852 Values available"),
        MapPointData("25.8", Point.fromLngLat(0.6430, 41.6755), "Torrefarrera", "156 Values available")
    )
    var selectedPoint by remember { mutableStateOf<MapPointData?>(null) }


    mainViewModel.isLoading.observe(context) {
        requestingPermissions = it
    }
    mainViewModel.appTheme.observe(context) {
        deviceTheme = it
    }
    LaunchedEffect(Unit){
        Log.d("HomeScreen", "Saved theme:${deviceTheme}")
    }
    key(deviceTheme){
        if(requestingPermissions){
            Column(modifier =Modifier.fillMaxSize()){
                Text(text ="Accept permissions to display the map.")
                Button(onClick = { MainActivity.locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ); }) {
                    Text(text = "Accept permissions")
                }
            }
        }else{
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
                        center(Point.fromLngLat(0.6206, 41.6148))
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
            ){
                locations.forEach { location ->
                    ViewAnnotation(
                        options = viewAnnotationOptions {
                            geometry(location.point)
                            allowOverlap(false)
                        }
                    ) {
                        MapDataObject(data = location)
                    }
                }
            }
        }

    }
}

@Composable
fun MapDataObject(data: MapPointData){
    Box(modifier = Modifier
        .size(40.dp)
        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp)).border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
        Text(text = data.id, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
    }
}