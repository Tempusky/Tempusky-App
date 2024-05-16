package com.example.tempusky

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tempusky.core.broadcastReceivers.LocationUpdatesReceiver
import com.example.tempusky.core.viewModels.LocationViewModel
import com.example.tempusky.data.SettingsDataStore
import com.example.tempusky.data.SettingsValues
import com.example.tempusky.ui.MainScreen
import com.example.tempusky.ui.theme.TempuskyTheme
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.SettingsClient

class MainActivity : ComponentActivity() {

    val mainViewModel : MainViewModel by viewModels()
    val locatioViewModel: LocationViewModel by viewModels()
    private lateinit var dataStore : SettingsDataStore
    private var settings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStore = SettingsDataStore(this)
        locationViewModel = locatioViewModel
        locatioViewModel.setLastUpdateTime("")
        setContent {
            val savedTheme = dataStore.getTheme.collectAsState(initial = SettingsValues.DEFAULT_THEME)
            Log.d(TAG, "Saved theme: ${savedTheme.value}")
            TempuskyTheme(mainViewModel, if(savedTheme.value == SettingsValues.DEFAULT_THEME) isSystemInDarkTheme() else savedTheme.value == SettingsValues.DARK_THEME) {
                MainScreen(this, mainViewModel)
            }
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)
        mRequestingLocationUpdates = false
        context = this

        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                    mRequestingLocationUpdates = true
                    mainViewModel.setLoading(false)
                    Log.i(
                        TAG,
                        "User agreed to make precise required location settings changes, updates requested, starting location updates."
                    )
                    startLocationUpdates()
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                    mRequestingLocationUpdates = true
                    mainViewModel.setLoading(false)
                    Log.i(
                        TAG,
                        "User agreed to make coarse required location settings changes, updates requested, starting location updates."
                    )
                    startLocationUpdates()
                } permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                    // Background location access granted.
                    mRequestingLocationUpdates = true
                    mainViewModel.setLoading(false)
                    Log.i(
                        TAG,
                        "User agreed to make background required location settings changes, updates requested, starting location updates."
                    )
                    startLocationUpdates()
                }else -> {
                    if (mRequestingLocationUpdates && checkPermissions()) {
                        Log.d(TAG, "onStart: requesting location updates")
                        requestPermissions()
                        startLocationUpdates()
                    } else if (!checkPermissions() && !settings) {
                        Log.d(TAG, "onStart: requesting permissions")
                        requestPermissions()
                    }
                    // No location access granted.
                       Log.i(TAG, "User denied location access, updates not requested, starting location updates.")
                }
            }
        }
        notificationsPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    Log.d("Permissions", "$permissionName granted")
                } else {
                    Log.d("Permissions", "$permissionName denied")
                }
            }
        }

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.web_client_id))
            .requestProfile()
            .build()

        requestPermissions()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION), 56)
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = UPDATE_INTERVAL_IN_MILLISECONDS
            fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val intent = Intent(applicationContext, LocationUpdatesReceiver::class.java)
        intent.action = "com.google.android.gms.location.example.tempusky.action.PROCESS_UPDATES"
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            3,
            intent,
            PendingIntent.FLAG_MUTABLE
        )

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "startLocationUpdates: requesting location updates")
            mFusedLocationClient.requestLocationUpdates(locationRequest, pendingIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: called")
    }

    private fun checkPermissions(): Boolean {
        val permissionFineState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionCoarseState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val permissionBackground = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )


        return ((permissionFineState == PackageManager.PERMISSION_GRANTED) || (permissionCoarseState == PackageManager.PERMISSION_GRANTED) || (permissionBackground == PackageManager.PERMISSION_GRANTED))
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Add other permissions if needed, e.g., location
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            notificationsPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
           //Show snackbar
            settings = true
        } else {
            Log.i(TAG, "Requesting permission")

            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }
    }

    fun showSnackbar(text: String) {
        TODO("Implement the trigger")
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val REQUEST_CHECK_SETTINGS = 0x1
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000000  // 10000
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2
        private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates"
        private const val KEY_LOCATION = "location"
        private const val KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string"
        lateinit var mainViewModel: MainViewModel
        lateinit var locationViewModel: LocationViewModel
        lateinit var mFusedLocationClient: FusedLocationProviderClient
        lateinit var mSettingsClient: SettingsClient
        lateinit var context : MainActivity
        lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
        lateinit var notificationsPermissionLauncher: ActivityResultLauncher<Array<String>>
        var mRequestingLocationUpdates: Boolean = false
        lateinit var gso : GoogleSignInOptions
    }
}
