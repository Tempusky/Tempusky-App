package com.example.tempusky.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempusky.MainViewModel
import com.example.tempusky.data.SettingsDataStore
import com.example.tempusky.data.SettingsValues
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = SettingsDataStore(context)

    val savedLanguage = dataStore.getLanguage.collectAsState(initial = SettingsValues.DEFAULT_LANGUAGE)
    val savedTheme = dataStore.getTheme.collectAsState(initial = SettingsValues.DEFAULT_THEME)
    val savedNetwork = dataStore.getNetwork.collectAsState(initial = SettingsValues.DEFAULT_NETWORK)
    val savedTemperatureEnabled = dataStore.getTemperature.collectAsState(initial = SettingsValues.DEFAULT_TEMPERATURE)
    val savedPressureEnabled = dataStore.getPressure.collectAsState(initial = SettingsValues.DEFAULT_PRESSURE)
    val savedHumidityEnabled = dataStore.getHumidity.collectAsState(initial = SettingsValues.DEFAULT_HUMIDITY)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Text("General", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 30.sp)
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Language", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                LanguageSelector(
                    languages = SettingsValues.LANGUAGES,
                    selectedLanguage = savedLanguage.value,
                    onLanguageSelected = { language ->
                        scope.launch {
                            dataStore.setLanguage(language)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Theme", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                ThemeSelector(
                    themes = SettingsValues.THEMES,
                    selectedTheme = savedTheme.value,
                    onThemeSelected = { theme ->
                        scope.launch {
                            dataStore.setTheme(theme)
                            mainViewModel.setAppTheme(theme)
                        }
                    }
                )

            }
            Spacer(modifier = Modifier.height(16.dp))
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Network", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                NetworkSelector(
                    networks = SettingsValues.NETWORKS,
                    selectedNetwork = savedNetwork.value,
                    onNetworkSelected = { network ->
                        scope.launch {
                            dataStore.setNetwork(network)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Sensors Data To Send", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 30.sp)
            SensorSwitch("Temperature", savedTemperatureEnabled.value) {
                scope.launch { dataStore.setTemperatureEnabled(it) }
            }
            SensorSwitch("Humidity", savedHumidityEnabled.value) {
                scope.launch { dataStore.setHumidityEnabled(it) }
            }
            SensorSwitch("Pressure", savedPressureEnabled.value) {
                scope.launch { dataStore.setPressureEnabled(it) }
            }
        }
    }
}

@Composable
fun LanguageSelector(
    languages: List<String>,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.selectableGroup()
    ) {
        languages.forEach { language ->
            Row(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .selectable(
                        selected = (language == selectedLanguage),
                        onClick = { onLanguageSelected(language) }
                    )
            ) {
                RadioButton(
                    selected = (language == selectedLanguage),
                    onClick = { onLanguageSelected(language) },
                    colors = RadioButtonDefaults.colors(selectedColor = androidx.compose.ui.graphics.Color.Blue)
                )
                Text(
                    text = language,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun SensorSwitch(sensorName: String, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$sensorName: ", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.weight(1f))
        androidx.compose.material3.Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle(it) }
        )
    }
}

@Composable
fun NetworkSelector(
    networks: List<String>,
    selectedNetwork: String,
    onNetworkSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.selectableGroup()
    ) {
        networks.forEach { network ->
            Row(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .selectable(
                        selected = (network == selectedNetwork),
                        onClick = { onNetworkSelected(network) }
                    )
            ) {
                RadioButton(
                    selected = (network == selectedNetwork),
                    onClick = { onNetworkSelected(network) },
                    colors = RadioButtonDefaults.colors(selectedColor = androidx.compose.ui.graphics.Color.Blue)
                )
                Text(
                    text = network,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun ThemeSelector(
    themes: List<String>,
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.selectableGroup()
    ) {
        themes.forEach { theme ->
            Row(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .selectable(
                        selected = (theme == selectedTheme),
                        onClick = { onThemeSelected(theme) }
                    )
            ) {
                RadioButton(
                    selected = (theme == selectedTheme),
                    onClick = { onThemeSelected(theme) },
                    colors = RadioButtonDefaults.colors(selectedColor = androidx.compose.ui.graphics.Color.Blue)
                )
                Text(
                    text = theme,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}