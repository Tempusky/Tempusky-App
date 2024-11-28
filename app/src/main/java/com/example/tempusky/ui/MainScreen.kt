package com.example.tempusky.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.tempusky.MainActivity
import com.example.tempusky.MainViewModel
import com.example.tempusky.ui.navigation.BottomNavBar
import com.example.tempusky.ui.navigation.TempuskyNavHost
import com.example.tempusky.ui.screens.search.SearchViewModel
import com.reown.appkit.ui.components.internal.AppKitComponent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    mainContext: MainActivity,
    mainViewModel: MainViewModel,
    searchViewModel: SearchViewModel
) {
    val navController = rememberNavController()
    var bottombarVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val appKitModalState = rememberModalBottomSheetState()

    mainViewModel.bottomBarVisibility.observe(mainContext) {
        bottombarVisible = it
    }

        Scaffold(
            bottomBar = {
                if (bottombarVisible) {
                    BottomNavBar(navController)
                }
            })
        {
            TempuskyNavHost(mainContext, navController = navController, mainViewModel, searchViewModel, appKitModalState)

        }

    if (appKitModalState.isVisible) {
        ModalBottomSheet(
            content = {
                Column {
                    AppKitComponent(
                        shouldOpenChooseNetwork = true,
                        closeModal = {coroutineScope.launch { appKitModalState.hide() } },
                    )
                }
            },
            sheetState = appKitModalState,
            onDismissRequest = { coroutineScope.launch { appKitModalState.hide() } },
        )
    }

}