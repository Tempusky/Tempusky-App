package com.example.tempusky.core.viewModels

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tempusky.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient

class LocationViewModel : ViewModel() {
    private var _latitude: MutableLiveData<Double> = MutableLiveData()
    val latitude: LiveData<Double> get() = _latitude

    private var _longitude: MutableLiveData<Double> = MutableLiveData()
    val longitude: LiveData<Double> get() = _longitude

    private var _lastUpdateTime: MutableLiveData<String> = MutableLiveData()
    val lastUpdateTime: LiveData<String> get() = _lastUpdateTime

    fun setLatitude(latitude: Double) {
        _latitude.value = latitude
    }

    fun setLongitude(longitude: Double) {
        _longitude.value = longitude
    }

    fun setLastUpdateTime(lastUpdateTime: String) {
        _lastUpdateTime.value = lastUpdateTime
    }

    fun setCurrentLocation(
        context: MainActivity, mFusedLocationClient: FusedLocationProviderClient
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //Ask permissions
            ActivityCompat.requestPermissions(
                context,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                101
            )

        }

        mFusedLocationClient.lastLocation.addOnSuccessListener { location ->
            // Got last known location. In some rare situations this can be null.
            setLatitude(
                location?.latitude!!
            )
            setLongitude(
                location.longitude
            )
        }
            .addOnFailureListener {
                context.showSnackbar("Failed on getting current location")
            }
    }

}