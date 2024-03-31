package com.example.tempusky.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.example.tempusky.domain.appNavigation.NavigationRoutes
import com.example.tempusky.ui.screens.home.HomeScreen
import com.example.tempusky.ui.screens.login.LoginScreen
import com.example.tempusky.ui.screens.profile.ProfileScreen
import com.example.tempusky.ui.screens.search.SearchScreen
import com.example.tempusky.ui.screens.settings.SettingsScreen
import com.example.tempusky.ui.screens.signup.SignupScreen

@Composable
fun TempuskyNavHost(navController: NavController) {
    NavHost(startDestination = NavigationRoutes.LOGIN, navController = navController as NavHostController) {
        composable(NavigationRoutes.LOGIN){
            LoginScreen(navController = navController)
        }
        composable(NavigationRoutes.HOME){
            HomeScreen()
        }
        composable(NavigationRoutes.PROFILE){
            ProfileScreen()
        }
        composable(NavigationRoutes.SETTINGS){
            SettingsScreen()
        }
        composable(NavigationRoutes.SIGNUP){
            SignupScreen()
        }
        composable(NavigationRoutes.SEARCH){
            SearchScreen()
        }
    }
}