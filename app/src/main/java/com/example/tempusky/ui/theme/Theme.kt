package com.example.tempusky.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
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
    mainViewModel: MainViewModel,
    darkTheme: Boolean,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    mainViewModel.setAppTheme(if (darkTheme) SettingsValues.DARK_THEME else SettingsValues.LIGHT_THEME)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}