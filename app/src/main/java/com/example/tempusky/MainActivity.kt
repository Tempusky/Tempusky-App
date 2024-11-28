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
import com.example.tempusky.ui.screens.search.SearchViewModel
import com.example.tempusky.ui.theme.TempuskyTheme
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.SettingsClient
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.presets.AppKitChainsPresets

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()
    private lateinit var dataStore: SettingsDataStore
    private var settings = false
    private lateinit var geofencingClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStore = SettingsDataStore(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        Companion.context = this@MainActivity
        setContent {
            val savedTheme =
                dataStore.getTheme.collectAsState(initial = SettingsValues.DEFAULT_THEME)
            Log.d(TAG, "Saved theme: ${savedTheme.value}")
            TempuskyTheme(
                mainViewModel,
                if (savedTheme.value == SettingsValues.DEFAULT_THEME) isSystemInDarkTheme() else savedTheme.value == SettingsValues.DARK_THEME
            ) {
                MainScreen(this, mainViewModel, searchViewModel = searchViewModel)
            }
        }

        initializeAppKitSingleton()
        initializeLocationComponents()
        initializePermissionLaunchers()
        requestPermissions()
    }

    private fun initializeLocationComponents() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)
        mRequestingLocationUpdates = false
    }

    private fun initializePermissionLaunchers() {
        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            handleLocationPermissionsResult(permissions)
        }

        notificationsPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            handleNotificationPermissionsResult(permissions)
        }

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.web_client_id))
            .requestProfile()
            .build()
    }

    private fun initializeAppKitSingleton() {
        // Initialize the AppKit singleton
        val connectionType = ConnectionType.AUTOMATIC
        val projectId = BuildConfig.REOWN_PROJECT_ID
        val appMetadata = Core.Model.AppMetaData(
            name = "Tempusky",
            description = "A descentralized weather app",
            url = "https://tempusky.com",
            icons = listOf("https://tempusky.com/icon.png"),
            redirect = "kotlin-modal-wc://request",
        )

        val errorCallback: (Core.Model.Error) -> Unit = { error ->
            // Handle the error here, for example:
            println("Error occurred: $error")
        }

        CoreClient.initialize(
            application = application,
            connectionType = connectionType,
            projectId = projectId,
            metaData = appMetadata,
            onError = {
                Log.e(TAG, "Error initializing CoreClient: $it")
            }
        )

        AppKit.initialize(
            init = Modal.Params.Init(CoreClient),
            onSuccess = {
                Log.d(TAG, "AppKit initialized successfully")
            },
            onError = {
                Log.e(TAG, "Error initializing AppKit: $it")
                // If we can't initialize AppKit, we have to disable the wallet feature
            }
        )

        val chainsList: List<Modal.Model.Chain> = AppKitChainsPresets.ethChains.values.toList()

        // Set the chains, we can define what chains we want to use, even custom ones
        // product/appkit/src/main/kotlin/com/reown/appkit/presets/AppKitChainsPresets.kt
        AppKit.setChains(chainsList)
    }

    private fun handleLocationPermissionsResult(permissions: Map<String, Boolean>) {
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                Log.i(TAG, "Precise location access granted.")
                startLocationUpdates()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Log.i(TAG, "Approximate location access granted.")
                startLocationUpdates()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                Log.i(TAG, "Background location access granted.")
                startLocationUpdates()
            }

            else -> {
                Log.i(TAG, "Location access denied.")
                if (mRequestingLocationUpdates && checkPermissions()) {
                    requestPermissions()
                } else if (!checkPermissions()) {
                    requestPermissions()
                }
            }
        }
    }

    private fun handleNotificationPermissionsResult(permissions: Map<String, Boolean>) {
        permissions.entries.forEach { (permissionName, isGranted) ->
            Log.d("Permissions", "$permissionName ${if (isGranted) "granted" else "denied"}")
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mainViewModel.setLoading(true)
        }
        val locationRequest = LocationRequest.create().apply {
            interval = UPDATE_INTERVAL_IN_MILLISECONDS
            fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val intent = Intent(applicationContext, LocationUpdatesReceiver::class.java).apply {
            action = "com.google.android.gms.location.example.tempusky.action.PROCESS_UPDATES"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            3,
            intent,
            PendingIntent.FLAG_MUTABLE
        )

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Requesting location updates")
            mFusedLocationClient.requestLocationUpdates(locationRequest, pendingIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: called")
    }

    private fun checkPermissions(): Boolean {
        val permissionFineState =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionCoarseState =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val permissionBackground =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        return permissionFineState == PackageManager.PERMISSION_GRANTED ||
                permissionCoarseState == PackageManager.PERMISSION_GRANTED ||
                permissionBackground == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )

        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

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

        } else {
            Log.i(TAG, "Requesting permission")
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }

    fun showSnackbar(text: String) {
        TODO("Implement the trigger")
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val REQUEST_CHECK_SETTINGS = 0x1
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1200000
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2
        lateinit var mainViewModel: MainViewModel
        lateinit var locationViewModel: LocationViewModel
        lateinit var mFusedLocationClient: FusedLocationProviderClient
        lateinit var mSettingsClient: SettingsClient
        lateinit var context: MainActivity
        lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
        lateinit var notificationsPermissionLauncher: ActivityResultLauncher<Array<String>>
        var mRequestingLocationUpdates: Boolean = false
        lateinit var gso: GoogleSignInOptions
    }
}
