package com.example.tempusky.ui.screens.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.tempusky.MainActivity
import com.example.tempusky.MainViewModel
import com.example.tempusky.core.broadcastReceivers.LocationProviderChangeReceiver
import com.example.tempusky.core.helpers.GeofencesHelper
import com.example.tempusky.core.services.FirebaseNotificationService
import com.example.tempusky.data.AverageDataLocation
import com.example.tempusky.data.MapLocations
import com.example.tempusky.data.SearchDataResult
import com.example.tempusky.ui.screens.search.SearchViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.mapbox.geojson.Point
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.DefaultSettingsProvider
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(MapboxExperimental::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    context: MainActivity,
    mainViewModel: MainViewModel,
    searchViewModel: SearchViewModel
) {
    val darkThemeMap = "mapbox://styles/faysalbadaoui/cluikavl200jr01r2hsgu2ejc"
    val lightThemeMap = "mapbox://styles/mapbox/outdoors-v12"
    var deviceTheme by remember { mutableStateOf("") }
    var requestingPermissions by remember { mutableStateOf(false) }
    var selectedPoint by remember { mutableStateOf<MapLocations?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var geofencesList by remember { mutableStateOf(listOf<MapLocations>()) }
    var resultsCity by remember { mutableStateOf(listOf<SearchDataResult>()) }
    var averageDataLocation by remember { mutableStateOf(listOf<AverageDataLocation>()) }
    var isLocationEnabled by remember { mutableStateOf(false) }
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val auth: FirebaseAuth = Firebase.auth
    val db: FirebaseFirestore = Firebase.firestore
    val requestLocationServices = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("TAG", "Location permission granted")
            isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }
    val backgroundLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    mainViewModel.averageData.observe(context) {
        averageDataLocation = it
    }
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
    val locationProviderChangeReceiver = rememberUpdatedState(
        LocationProviderChangeReceiver {
            isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    )
    DisposableEffect(Unit) {
        val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        context.registerReceiver(locationProviderChangeReceiver.value, intentFilter)
        onDispose {
            Log.d("TAG", "Disposing")
            mainViewModel.showBottomSheet(false)
        }
    }
    LaunchedEffect(Unit) {
        requestLocationServices.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        mainViewModel.getGeofencesCloud(context)
        if (auth.currentUser != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(
                        FirebaseNotificationService.TAG,
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    return@addOnCompleteListener
                }
                val token = task.result
                db.collection("user_tokens").document(auth.currentUser!!.uid).set(
                    hashMapOf(
                        "token" to token
                    )
                )
                FirebaseNotificationService.token = token.toString()
                Log.d(FirebaseNotificationService.TAG, "FCM token: $token")
            }

        }
        while (!requestingPermissions) {
            mainViewModel.setLoading(backgroundLocationPermission == PackageManager.PERMISSION_GRANTED)
            Log.d("TAG", "Requesting permissions: $requestingPermissions")
            delay(2000)
        }
        GeofencesHelper.initialize(context)
        for (geofence in geofencesList) {
            GeofencesHelper.addGeofence(
                context,
                geofence.location,
                geofence.latitude.toDouble(),
                geofence.longitude.toDouble(),
                200f
            )
        }
    }
    key(deviceTheme) {
        if (!requestingPermissions || !isLocationEnabled) {
            if (!requestingPermissions) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(text = "Accept permissions to display the map. And enable background location with Geofences to contribute to the data.")
                    Button(onClick = {
                        MainActivity.locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                        )
                    }) {
                        Text(text = "Accept permissions")
                    }
                }
            } else if (!isLocationEnabled) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(text = "Please turn on Location Services to use the app and enjoy the GeoFences and to contribute to the data.")
                    Button(onClick = {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }) {
                        Text("Turn On Location")
                    }
                }
            }
        } else {

            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapInitOptionsFactory = { context ->
                    MapInitOptions(
                        context = context,
                        styleUri = if (deviceTheme == "Dark") darkThemeMap else lightThemeMap,
                    )
                },
                mapViewportState = MapViewportState().apply {
                    setCameraOptions {
                        zoom(11.0)
                        center(Point.fromLngLat(0.6206, 41.6148))
                        pitch(0.0)
                        bearing(0.0)
                    }
                    style {
                        Style.DARK
                    }
                },
                locationComponentSettings = DefaultSettingsProvider.defaultLocationComponentSettings(
                    context = LocalContext.current
                ).toBuilder()
                    .setLocationPuck(createDefault2DPuck(withBearing = false))
                    .setEnabled(true)
                    .build()
            ) {
                geofencesList.forEach { location ->
                    val avgData = averageDataLocation.find { it.location == location.location }
                    ViewAnnotation(
                        options = viewAnnotationOptions {
                            geometry(
                                Point.fromLngLat(
                                    location.longitude.toDouble(),
                                    location.latitude.toDouble()
                                )
                            )
                            allowOverlap(true)
                            allowOverlapWithPuck(true)
                        }
                    ) {
                        MapDataObject(
                            data = location,
                            avgData,
                            viewModel = mainViewModel,
                            searchViewModel = searchViewModel
                        )
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
                    val avgData =
                        averageDataLocation.find { it.location == selectedPoint?.location }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier.heightIn(600.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Place,
                                    contentDescription = "date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = selectedPoint?.location ?: "No data selected",
                                    fontSize = 35.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .padding(end = 20.dp)
                                )
                                if (avgData != null) {
                                    Text(
                                        text = avgData.averageTemp + "ºC",
                                        fontSize = 30.sp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 10.dp)
                            ) {
                                item(1)
                                {
                                    Text(
                                        text = "Data received from ${selectedPoint?.location}:",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                item {
                                    Button(onClick = {
                                        db.collection("requests").add(
                                            hashMapOf(
                                                "location" to selectedPoint?.location
                                            )
                                        )
                                    }) {
                                        Text(text = "Request data!")
                                    }
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
fun DataCityItem(result: SearchDataResult) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = result.location, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = "Temperature: ${result.temperature}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Humidity: ${result.humidity}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Pressure: ${result.pressure}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = result.date, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun MapDataObject(
    data: MapLocations,
    averageData: AverageDataLocation?,
    viewModel: MainViewModel,
    searchViewModel: SearchViewModel
) {
    Box(modifier = Modifier
        .size(40.dp)
        .clickable {
            viewModel.setSelectedMapData(data); searchViewModel.updateSearchDataResult(
            data.location
        )
        }
        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center) {
        if (averageData != null) {
            Text(
                text = averageData.averageTemp,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
        }

    }
}