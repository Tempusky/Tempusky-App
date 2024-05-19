package com.example.tempusky

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import com.google.firebase.storage.ktx.storage
import java.io.File

class MainViewModel : ViewModel() {

    val db = Firebase.firestore
    val auth = Firebase.auth

    private var _bottomBarVisibility = MutableLiveData(false)
    val bottomBarVisibility : LiveData<Boolean> = _bottomBarVisibility

    private var _isLoading = MutableLiveData(false)
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

    fun getStorageFile(context: Context) {
        // Check permission to read external storage
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(context as MainActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            return
        }

        val storage = Firebase.storage
        val storageRef = storage.reference
        val pathReference = storageRef.child("/Dummy export test - Tempusky.pdf")

        // Create a file in the external files directory with the .pdf extension
        val localFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Contributions.pdf")

        pathReference.getFile(localFile).addOnSuccessListener {
            Log.d("MainViewModel", "File saved on device")
            Log.d("MainViewModel", "File path: ${localFile.absolutePath}")
            Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show()
            openFile(context, localFile)
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to download file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No app available to open this file", Toast.LENGTH_SHORT).show()
        }
    }

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