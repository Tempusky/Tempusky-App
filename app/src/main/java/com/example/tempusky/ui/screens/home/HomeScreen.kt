package com.example.tempusky.ui.screens.home

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempusky.core.helpers.GeofencesHelper
import com.example.tempusky.data.GeofenceData
import com.example.tempusky.data.SearchDataResult
import com.example.tempusky.ui.screens.search.DataReceivedItem
import com.example.tempusky.ui.screens.search.SearchViewModel
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(MapboxExperimental::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(context: MainActivity, mainViewModel: MainViewModel, searchViewModel: SearchViewModel) {
    val darkThemeMap = "mapbox://styles/faysalbadaoui/cluikavl200jr01r2hsgu2ejc"
    val lightThemeMap = "mapbox://styles/mapbox/outdoors-v12"
    var deviceTheme by remember { mutableStateOf("") }
    var requestingPermissions by remember { mutableStateOf(false) }
    val locations = listOf(
        MapPointData("24.5", Point.fromLngLat(0.6177, 41.6177), "Lleida", "1221 Values available"),
        MapPointData("23", Point.fromLngLat(0.5730, 41.7296), "Albesa", "223 Values available"),
        MapPointData("22.3", Point.fromLngLat(0.8114, 41.7910), "Balaguer", "852 Values available"),
        MapPointData("25.8", Point.fromLngLat(0.6430, 41.6755), "Torrefarrera", "156 Values available")
    )
    var selectedPoint by remember { mutableStateOf<MapPointData?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var geofencesList by remember { mutableStateOf(listOf<GeofenceData>())}
    var resultsCity by remember { mutableStateOf(listOf<SearchDataResult>())}
    mainViewModel.geoFences.observe(context) {
        geofencesList = it
    }
    searchViewModel.searchDataResult.observe(context) {
        resultsCity = it
    }
    mainViewModel.selectedMapData.observe(context) {
        selectedPoint = it
    }
    mainViewModel.showBottomSheet.observe(context) {
        showBottomSheet = it
    }
    mainViewModel.isLoading.observe(context) {
        requestingPermissions = it
    }
    mainViewModel.appTheme.observe(context) {
        deviceTheme = it
    }

    DisposableEffect(Unit){
        onDispose {
            mainViewModel.showBottomSheet(false)
        }
    }
    LaunchedEffect(Unit){
        GeofencesHelper.initialize(context)
        mainViewModel.getGeofencesCloud(context)
    }
    LaunchedEffect(geofencesList)
    {
        for (geofence in geofencesList) {
            GeofencesHelper.addGeofence(context, geofence.id, geofence.location_coords.latitude, geofence.location_coords.longitude, geofence.radius)
        }
    }
    key(deviceTheme){
        if(requestingPermissions){
            Column(modifier =Modifier.fillMaxSize()){
                Text(text ="Accept permissions to display the map.")
                Button(onClick = { MainActivity.locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
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
                            allowOverlap(true)
                            allowOverlapWithPuck(true)
                        }
                    ) {
                        MapDataObject(data = location, viewModel = mainViewModel,searchViewModel = searchViewModel)
                    }
                }
            }
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState
                ) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Column(modifier = Modifier.heightIn(600.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
                                Icon(imageVector = Icons.Filled.Place, contentDescription = "date", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                Text(text = selectedPoint?.title ?: "No data selected", fontSize = 35.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
                                Icon(imageVector = Icons.Outlined.Info, contentDescription = "date", tint = MaterialTheme.colorScheme.primary, modifier = Modifier
                                    .size(50.dp)
                                    .padding(end = 20.dp))
                                Text(text = selectedPoint?.id + "ºC", fontSize = 30.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                            }
                            LazyColumn(modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 10.dp)) {
                                item(1)
                                {
                                    Text(text = "Data received from ${selectedPoint?.title}:", fontSize = 20.sp, fontWeight = FontWeight.Normal)
                                }
                                items(resultsCity.size)
                                {
                                    DataCityItem(resultsCity[it])
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun DataCityItem(result: SearchDataResult){
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)
        .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)){
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = result.location, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "Temperature: ${result.temperature}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "Humidity: ${result.humidity}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "Pressure: ${result.pressure}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = result.date, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun MapDataObject(data: MapPointData, viewModel: MainViewModel, searchViewModel: SearchViewModel){
    Box(modifier = Modifier
        .size(40.dp)
        .clickable { viewModel.setSelectedMapData(data); searchViewModel.updateSearchDataResult(data.title) }
        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
        Text(text = data.id, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
    }
}