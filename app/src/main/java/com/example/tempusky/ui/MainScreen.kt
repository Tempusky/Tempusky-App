package com.example.tempusky.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.tempusky.MainActivity
import com.example.tempusky.MainViewModel
import com.example.tempusky.ui.navigation.BottomNavBar
import com.example.tempusky.ui.navigation.TempuskyNavHost

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(context: MainActivity, mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    var bottombarVisible by remember { mutableStateOf(false) }

    mainViewModel.bottomBarVisibility.observe(context) {
        bottombarVisible = it
    }

    Scaffold(
        bottomBar = {
            if(bottombarVisible){
                BottomNavBar(navController)
            }
        })
    {
        TempuskyNavHost(navController = navController)
    }
}