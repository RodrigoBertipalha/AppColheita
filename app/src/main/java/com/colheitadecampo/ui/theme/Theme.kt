package com.colheitadecampo.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.colheitadecampo.ui.theme.BluePrimary
import com.colheitadecampo.ui.theme.BlueMedium
import com.colheitadecampo.ui.theme.TerracotaAccent
import com.colheitadecampo.ui.theme.LimeAccent
import com.colheitadecampo.ui.theme.NeutralBlack
import com.colheitadecampo.ui.theme.NeutralGray
import com.colheitadecampo.ui.theme.NeutralLightGray
import com.colheitadecampo.ui.theme.Success
import com.colheitadecampo.ui.theme.Error
import com.colheitadecampo.ui.theme.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = White,
    primaryContainer = Color(0xFFCFE3EF), // Versão clara do BluePrimary
    onPrimaryContainer = BluePrimary,
    secondary = BlueMedium,
    onSecondary = White,
    secondaryContainer = Color(0xFFD1E4FF),
    onSecondaryContainer = BlueMedium,
    tertiary = TerracotaAccent,
    onTertiary = White,
    background = White,
    onBackground = NeutralBlack,
    surface = White,
    onSurface = NeutralBlack,
    surfaceVariant = NeutralLightGray,
    onSurfaceVariant = NeutralGray,
    error = Error,
    onError = White
)

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = White,
    primaryContainer = Color(0xFF0A3D59), // Versão mais escura do BluePrimary
    onPrimaryContainer = White,
    secondary = BlueMedium,
    onSecondary = White,
    secondaryContainer = Color(0xFF002673), // Versão mais escura do BlueMedium
    onSecondaryContainer = White,
    tertiary = TerracotaAccent,
    onTertiary = NeutralBlack,
    background = Color(0xFF121212),
    onBackground = NeutralLightGray,
    surface = Color(0xFF121212),
    onSurface = NeutralLightGray,
    surfaceVariant = NeutralGray,
    onSurfaceVariant = NeutralLightGray,
    error = Error,
    onError = White
)

@Composable
fun ColheitaDeCampoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
