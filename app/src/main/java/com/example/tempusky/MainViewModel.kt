package com.example.tempusky

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tempusky.data.AverageDataLocation
import com.example.tempusky.data.MapLocations
import com.example.tempusky.data.SettingsValues
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainViewModel : ViewModel() {

    val db = Firebase.firestore
    val auth = Firebase.auth

    private var _bottomBarVisibility = MutableLiveData(false)
    val bottomBarVisibility : LiveData<Boolean> = _bottomBarVisibility

    private var _isLoading = MutableLiveData(true)
    val isLoading : LiveData<Boolean> = _isLoading

    private var _isDarkTheme = MutableLiveData("")
    val appTheme : LiveData<String> = _isDarkTheme

    private var _showBottomSheet = MutableLiveData(false)
    val showBottomSheet : LiveData<Boolean> = _showBottomSheet

    private var _selectedMapData: MutableLiveData<MapLocations> = MutableLiveData()
    val selectedMapData: LiveData<MapLocations> = _selectedMapData

    private var _geoFences = MutableLiveData<List<MapLocations>>()
    val geoFences : LiveData<List<MapLocations>> = _geoFences

    private var _averageData = MutableLiveData<List<AverageDataLocation>>()
    val averageData : LiveData<List<AverageDataLocation>> = _averageData

    fun getGeofencesCloud(context : MainActivity){
        val locationsOnData = mutableListOf<MapLocations>()
        val savedLocation = mutableListOf<String>()
        db.collection("environment_sensors_data").get()
            .addOnSuccessListener { data ->
                for (document in data) {
                    val data = document.data
                    if (data["location_cords"] != null && data["location"] != null){
                    val location = data["location"].toString()
                        val latitude = (data["location_cords"] as GeoPoint).latitude.toString()
                        val longitude = (data["location_cords"] as GeoPoint).longitude.toString()
                        if(!savedLocation.contains(location)){
                            savedLocation.add(location)
                            locationsOnData.add(
                                MapLocations(
                                    location,
                                    latitude,
                                    longitude
                                )
                            )
                            saveAverageDataFromLocation(location)
                        }
                    }
                }
                _geoFences.value = locationsOnData
            }
    }

    fun saveAverageDataFromLocation(location : String){
        var averageTemp = 0.0
        var averagePressure = 0.0
        var averageHumidity = 0.0
        var countTemp = 0
        var countPressure = 0
        var countHumidity = 0
        db.collection("environment_sensors_data").get()
            .addOnSuccessListener { data1 ->
                for (document in data1) {
                    val data = document.data
                    val location2 = data["location"].toString()
                    if(location2 == location){
                        val temperature = data["temperature"]?.toString()?.toDouble()
                        val pressure = data["pressure"]?.toString()?.toDouble()
                        val humidity = data["humidity"]?.toString()?.toDouble()
                        temperature?.let { averageTemp += it; countTemp++ }
                        pressure?.let { averagePressure += it; countPressure++ }
                        humidity?.let { averageHumidity += it; countHumidity++ }
                    }
                }
                //max 1 decimal
                averageTemp /= countTemp
                averagePressure /= countPressure
                averageHumidity /= countHumidity
                val tempList = _averageData.value?.toMutableList() ?: mutableListOf()
                tempList.add(
                    AverageDataLocation(
                        location,
                        String.format("%.1f", averageTemp),
                        String.format("%.1f", averagePressure),
                        String.format("%.1f", averageHumidity)
                    )
                )
                _averageData.value = tempList
            }
    }

    fun setSelectedMapData(data : MapLocations){
        _selectedMapData.value = data
        showBottomSheet(true)
    }

    fun showBottomSheet(v : Boolean){
        _showBottomSheet.value = v
    }

    fun setLoading(v : Boolean){
        _isLoading.value = v
    }

    fun setAppTheme(v : String){
        if(v == SettingsValues.DEFAULT_THEME) return
        _isDarkTheme.value = v
    }

    fun setBottomBarVisible(v : Boolean){
        _bottomBarVisibility.value = v
    }

    fun signOut(){
        Firebase.auth.signOut()
        setBottomBarVisible(false)
    }
}