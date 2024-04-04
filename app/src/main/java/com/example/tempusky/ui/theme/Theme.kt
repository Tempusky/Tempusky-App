package com.example.tempusky.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.tempusky.MainActivity
import com.example.tempusky.MainViewModel
import com.example.tempusky.data.SettingsValues

val DarkColorScheme = darkColorScheme(
    background = Color(0xFF292929),
    primary = Purple80,
    secondary = PurpleGrey40,
    tertiary = Pink80,
    onBackground = Color(0xFFFFFFFF),

)

val LightColorScheme = lightColorScheme(
    background = Color(0xFFFBFBFB),
    primary = Purple40,
    secondary = PurpleGrey80,
    tertiary = Pink40,
    onBackground = Color(0xFF272727),
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun TempuskyTheme(
    context: MainActivity,
    mainViewModel: MainViewModel,
    content: @Composable () -> Unit
) {
    var appTheme by remember { mutableStateOf("") }
    mainViewModel.appTheme.observe(context) {
        appTheme = it
    }
    val colorScheme = when {
        appTheme == SettingsValues.DARK_THEME -> DarkColorScheme
        appTheme == SettingsValues.LIGHT_THEME -> LightColorScheme
        else -> {
            if (isSystemInDarkTheme()) DarkColorScheme
            else LightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}