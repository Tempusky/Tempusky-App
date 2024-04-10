package com.example.tempusky.ui

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.tempusky.MainActivity
import com.example.tempusky.MainViewModel
import com.example.tempusky.ui.navigation.BottomNavBar
import com.example.tempusky.ui.navigation.TempuskyNavHost

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(mainContext: MainActivity, mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    var bottombarVisible by remember { mutableStateOf(false) }

    mainViewModel.bottomBarVisibility.observe(mainContext) {
        bottombarVisible = it
    }

    Scaffold(
        bottomBar = {
            if(bottombarVisible){
                BottomNavBar(navController)
            }
        })
    {
        TempuskyNavHost(mainContext, navController = navController, mainViewModel)
    }
}