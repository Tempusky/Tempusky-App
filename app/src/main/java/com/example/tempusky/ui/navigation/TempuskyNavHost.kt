package com.example.tempusky.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.example.tempusky.domain.appNavigation.NavigationRoutes

@Composable
fun TempuskyNavHost(navController: NavController) {
    NavHost(startDestination = NavigationRoutes.LOGIN, navController = navController as NavHostController) {
        composable(NavigationRoutes.LOGIN){

        }
    }
}