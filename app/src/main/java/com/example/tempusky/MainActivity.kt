package com.example.tempusky

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.tempusky.data.SettingsDataStore
import com.example.tempusky.data.SettingsValues
import com.example.tempusky.ui.MainScreen
import com.example.tempusky.ui.theme.TempuskyTheme

class MainActivity : ComponentActivity() {

    val mainViewModel : MainViewModel by viewModels()
    private lateinit var dataStore : SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStore = SettingsDataStore(this)
        setContent {
            mainViewModel.setAppTheme(dataStore.getTheme.collectAsState(initial = SettingsValues.DEFAULT_THEME).value)
            TempuskyTheme(this, mainViewModel) {
                MainScreen(this, mainViewModel)
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
