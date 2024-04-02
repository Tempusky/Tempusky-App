package com.example.tempusky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.tempusky.data.SettingsDataStore
import com.example.tempusky.ui.MainScreen
import com.example.tempusky.ui.theme.TempuskyTheme

class MainActivity : ComponentActivity() {

    val mainViewModel : MainViewModel by viewModels()
    private lateinit var dataStore : SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStore = SettingsDataStore(this)

        setContent {
            TempuskyTheme {
                MainScreen(this, mainViewModel, dataStore)
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
