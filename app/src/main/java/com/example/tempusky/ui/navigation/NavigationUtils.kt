package com.example.tempusky.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tempusky.domain.appNavigation.NavigationRoutes

@Composable
fun currentRoute(navHostController: NavHostController): String? =
    navHostController.currentBackStackEntryAsState().value?.destination?.route

sealed class NavigationItem(var route: String, val icon: ImageVector) {
    object Home : NavigationItem(
        NavigationRoutes.HOME,
        Icons.Default.Home
    )

    object Profile : NavigationItem(
        NavigationRoutes.PROFILE,
        Icons.Default.Person
    )

    object Search : NavigationItem(
        NavigationRoutes.SEARCH,
        Icons.Default.Search
    )
}