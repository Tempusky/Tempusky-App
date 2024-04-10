package com.example.tempusky

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.core.app.ActivityCompat
import com.example.tempusky.core.services.LocationForegroundService
import com.example.tempusky.core.viewModels.LocationViewModel
import com.example.tempusky.data.SettingsDataStore
import com.example.tempusky.data.SettingsValues
import com.example.tempusky.ui.MainScreen
import com.example.tempusky.ui.theme.TempuskyTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.SettingsClient
import com.google.android.material.snackbar.Snackbar

class MainActivity : ComponentActivity() {

    val mainViewModel : MainViewModel by viewModels()
    val locatioViewModel: LocationViewModel by viewModels()
    private lateinit var dataStore : SettingsDataStore
    private var settings = false

    @RequiresApi(Build.VERSION_CODES.O)
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

        //mCurrentLocation = Location()
        //mCurrentLocation.latitude = 41.6082
        //mCurrentLocation.longitude = 0.6231

        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                    mRequestingLocationUpdates = true
                    Log.i(TAG, "User agreed to make precise required location settings changes, updates requested, starting location updates.")
                    val foregroundIntent = Intent(this, LocationForegroundService::class.java)
                    startForegroundService(foregroundIntent)
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                    mRequestingLocationUpdates = true
                    Log.i(TAG, "User agreed to make coarse required location settings changes, updates requested, starting location updates.")
                    val foregroundIntent = Intent(this, LocationForegroundService::class.java)
                    startForegroundService(foregroundIntent)
                } else -> {
                    if (mRequestingLocationUpdates && checkPermissions()) {
                        Log.d(TAG, "onStart: requesting location updates")
                        val foregroundIntent = Intent(this, LocationForegroundService::class.java)
                        startForegroundService(foregroundIntent)
                    } else if (!checkPermissions() && !settings) {
                        Log.d(TAG, "onStart: requesting permissions")
                        requestPermissions()
                    }
                    // No location access granted.
                TODO("Show snackbkar or alert that permissions not granted")
                Log.d(TAG, "User agreed to make required location settings changes, updates requested, starting location updates.")
                    //>Show snacbr that permissions not allowed
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: called")
    }

    private fun stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.")
            return
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        //mFusedLocationClient.removeLocationUpdates(mLocationCallback)
        mRequestingLocationUpdates = false
        stopService(Intent(this, LocationForegroundService::class.java))

    }

    @RequiresApi(Build.VERSION_CODES.O)
    public override fun onResume() {
        super.onResume()
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (checkPermissions()) {
            val foregroundIntent = Intent(this, LocationForegroundService::class.java)
            startForegroundService(foregroundIntent)
        }

    }

    override fun onPause() {
        super.onPause()

        // Remove location updates to save battery.
        if (mRequestingLocationUpdates)
            stopLocationUpdates()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates)
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Shows a [Snackbar].
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private fun showSnackbar(
        mainTextStringId: Int, actionStringId: Int,
        listener: View.OnClickListener
    ) {
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(mainTextStringId),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(getString(actionStringId), listener).show()
    }

    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        val permissionFineState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionCoarseState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        return ((permissionFineState == PackageManager.PERMISSION_GRANTED) || (permissionCoarseState == PackageManager.PERMISSION_GRANTED))
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
           //Show snackbar
            settings = true
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".


        }
    }

    fun showSnackbar(text: String) {
        TODO("Implement the trigger")
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val REQUEST_CHECK_SETTINGS = 0x1
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 100  // 10000
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
        var mRequestingLocationUpdates: Boolean = false
    }
}
