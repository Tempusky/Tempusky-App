package com.example.tempusky.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun currentRoute(navHostController: NavHostController) : String? = navHostController.currentBackStackEntryAsState().value?.destination?.route