package com.example.tempusky.ui

import androidx.compose.foundation.background
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.tempusky.ui.navigation.TempuskyNavHost

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    TempuskyNavHost(navController = navController)
}