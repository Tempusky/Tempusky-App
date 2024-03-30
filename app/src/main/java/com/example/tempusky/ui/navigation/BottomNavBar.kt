package com.example.tempusky.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.tempusky.domain.appNavigation.NavigationRoutes

@Composable
fun BottomNavBar(navController: NavController) {
    BottomAppBar {
        NavigationBarItem(selected = currentRoute(navHostController = navController as NavHostController) == NavigationRoutes.HOME , onClick = { navController.navigate(NavigationRoutes.HOME)}, icon = { Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Home Page"
        ) },
            label = {
                Text("Home")
        })
        NavigationBarItem(selected = currentRoute(navHostController = navController as NavHostController) == NavigationRoutes.SEARCH , onClick = { navController.navigate(NavigationRoutes.SEARCH) }, icon = { Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search Data Page"
        ) },
            label = {
                Text("Search")
        })
        NavigationBarItem(selected = currentRoute(navHostController = navController as NavHostController) == NavigationRoutes.PROFILE , onClick = { navController.navigate(NavigationRoutes.PROFILE) }, icon = { Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "User Profile Page"
        ) },
        label = {
            Text("Profile")
        })
    }
}