package com.example.tempusky.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStore(private val context: Context) {

    companion object {
        private const val SETTINGS_DATA_STORE_NAME = "settings_data_store"
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_DATA_STORE_NAME)
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val THEME_KEY = stringPreferencesKey("theme")
        val NETWORK_KEY = stringPreferencesKey("network")
        val TEMPERATURE_KEY = booleanPreferencesKey("temperature")
        val PRESSURE_KEY = booleanPreferencesKey("pressure")
        val HUMIDITY_KEY = booleanPreferencesKey("humidity")
    }

    val getLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: SettingsValues.DEFAULT_LANGUAGE
        }

    val getTheme: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_KEY] ?: SettingsValues.DEFAULT_THEME
        }

    val getTemperature: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[TEMPERATURE_KEY] ?: SettingsValues.DEFAULT_TEMPERATURE
        }

    val getPressure: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PRESSURE_KEY] ?: SettingsValues.DEFAULT_PRESSURE
        }

    val getHumidity: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HUMIDITY_KEY] ?: SettingsValues.DEFAULT_HUMIDITY
        }

    suspend fun setTemperatureEnabled(temperature: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TEMPERATURE_KEY] = temperature
        }
    }

    suspend fun setPressureEnabled(pressure: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PRESSURE_KEY] = pressure
        }
    }

    suspend fun setHumidityEnabled(humidity: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HUMIDITY_KEY] = humidity
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    val getNetwork: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[NETWORK_KEY] ?: SettingsValues.DEFAULT_NETWORK
        }

    suspend fun setNetwork(network: String) {
        context.dataStore.edit { preferences ->
            preferences[NETWORK_KEY] = network
        }
    }


}