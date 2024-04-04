package com.example.tempusky.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.tempusky.domain.appNavigation.NavigationRoutes

@Composable
fun BottomNavBar(navController: NavController) {
    val currentRoute = currentRoute(navController as NavHostController)
    val navItems = listOf(
        NavigationItem.Home,
        NavigationItem.Search,
        NavigationItem.Profile
    )

    val weight = 1f / navItems.size // Calculate equal weight for each item

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondary)
            .fillMaxWidth()
            .fillMaxHeight(0.07f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navItems.forEach { route ->
            val isSelected = route.route == currentRoute
            val iconSize = if (isSelected) 36.dp else 30.dp // Increase icon size if selected

            Box(
                modifier = Modifier
                    .weight(weight) // Apply equal weight to each item
                    .clickable { navController.navigate(route.route) }
            ) {
                NavBarItem(
                    isSelected = isSelected,
                    icon = route.icon
                )
            }
        }
    }
}

@Composable
fun NavBarItem(
    isSelected: Boolean,
    icon: ImageVector,
) {
    val color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Gray
    val iconSize = if (isSelected) 36.dp else 30.dp
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(5.dp))
            .height(3.dp).weight(0.1f)
            .fillMaxWidth(0.8f))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "",
                    tint = color,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }

}