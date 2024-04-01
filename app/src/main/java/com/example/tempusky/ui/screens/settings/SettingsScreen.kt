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
import com.example.tempusky.storage.SettingsDataStore
import com.example.tempusky.storage.SettingsValues
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = SettingsDataStore(context)

    val savedLanguage = dataStore.getLanguage.collectAsState(initial = SettingsValues.DEFAULT_LANGUAGE)
    val savedTheme = dataStore.getTheme.collectAsState(initial = SettingsValues.DEFAULT_THEME)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Language")
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
                Text("Theme")
                Spacer(modifier = Modifier.weight(1f))
                ThemeSelector(
                    themes = SettingsValues.THEMES,
                    selectedTheme = savedTheme.value,
                    onThemeSelected = { theme ->
                        scope.launch {
                            dataStore.setTheme(theme)
                        }
                    }
                )
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
                    modifier = Modifier.padding(start = 4.dp)
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
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}