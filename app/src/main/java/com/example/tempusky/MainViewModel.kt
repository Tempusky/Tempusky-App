package com.example.tempusky

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tempusky.core.helpers.GeofencesHelper
import com.example.tempusky.data.GeofenceData
import com.example.tempusky.data.SettingsValues
import com.example.tempusky.domain.map.MapPointData
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

    private var _selectedMapData: MutableLiveData<MapPointData> = MutableLiveData()
    val selectedMapData: LiveData<MapPointData> = _selectedMapData

    private var _geoFences = MutableLiveData<List<GeofenceData>>()
    val geoFences : LiveData<List<GeofenceData>> = _geoFences
    fun getGeofencesCloud(context : MainActivity){
        db.collection("geofences").get()
            .addOnSuccessListener { result ->
                val tempList = mutableListOf<GeofenceData>()
                for (document in result) {
                    val data = document.data
                    val mapData = GeofenceData(
                        data["geofence_id"].toString(),
                        data["location"].toString(),
                        data["location_cords"] as GeoPoint,
                        data["radius"].toString().toFloat()
                    )
                    tempList.add(mapData)
                }
                _geoFences.value = tempList
            }
    }
    fun setSelectedMapData(data : MapPointData){
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