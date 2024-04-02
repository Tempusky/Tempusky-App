package com.example.tempusky.data

import kotlinx.coroutines.flow.first

object SettingsHelper {
    var language: String = ""
    var theme: String = ""

    suspend fun setSettings(dataStore: SettingsDataStore){
        language = dataStore.getLanguage.first()
        theme = dataStore.getTheme.first()
    }
}