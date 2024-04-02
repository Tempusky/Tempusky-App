package com.example.tempusky

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
            val savedTheme = dataStore.getTheme.collectAsState(initial = SettingsValues.DEFAULT_THEME)
            Log.d(TAG, "Saved theme: ${savedTheme.value}")
            TempuskyTheme(savedTheme.value.equals("Dark")) {
                MainScreen(this, mainViewModel)
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
