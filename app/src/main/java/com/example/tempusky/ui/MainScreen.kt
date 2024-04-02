package com.example.tempusky.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.navigation.compose.rememberNavController
import com.example.tempusky.MainActivity
import com.example.tempusky.MainViewModel
import com.example.tempusky.data.SettingsDataStore
import com.example.tempusky.data.SettingsHelper
import com.example.tempusky.ui.navigation.BottomNavBar
import com.example.tempusky.ui.navigation.TempuskyNavHost

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(mainContext: MainActivity, mainViewModel: MainViewModel, dataStore: SettingsDataStore) {
    val navController = rememberNavController()
    var bottombarVisible by remember { mutableStateOf(false) }

    mainViewModel.bottomBarVisibility.observe(mainContext) {
        bottombarVisible = it
    }

    var _isLoading by remember{ mutableStateOf(true) }
    mainViewModel.isLoading.observe(mainContext) {
        _isLoading = it
    }

    Scaffold(
        bottomBar = {
            if(bottombarVisible){
                BottomNavBar(navController)
            }
        })
    {
        TempuskyNavHost(navController = navController, mainViewModel)
    }
}