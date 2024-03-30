package com.example.tempusky.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable

@Composable
fun BottomNavBar() {
    BottomAppBar {
        NavigationBarItem(selected = false , onClick = { /*TODO*/ }, icon = { Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Home Page"
        ) })
        NavigationBarItem(selected = false , onClick = { /*TODO*/ }, icon = { Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search Data Page"
        ) })
        NavigationBarItem(selected = false , onClick = { /*TODO*/ }, icon = { Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "User Profile Page"
        ) })
    }
}