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
    // Cores principais com contraste aumentado
    primary = BluePrimary,
    onPrimary = White,
    primaryContainer = Color(0xFFCFE3EF), // Versão clara do BluePrimary
    onPrimaryContainer = BluePrimary,
    
    // Secundária mais vibrante para melhor visibilidade
    secondary = HighContrastBlue, // Azul mais vibrante para maior visibilidade
    onSecondary = White,
    secondaryContainer = Color(0xFFD1E4FF),
    onSecondaryContainer = BlueMedium,
    
    // Terciária mais contrastante
    tertiary = HighVisibilityOrange, // Laranja de alta visibilidade
    onTertiary = BlackText,
    
    // Cores de fundo e superfície com contraste otimizado
    background = OffWhite, // Ligeiramente off-white para reduzir brilho em luz solar
    onBackground = BlackText,
    surface = White,
    onSurface = BlackText,
    surfaceVariant = NeutralLightGray,
    onSurfaceVariant = NeutralBlack, // Texto escuro para melhor visibilidade
    
    // Cores de erro mais saturadas
    error = Error,
    onError = White
)

private val DarkColorScheme = darkColorScheme(
    // Modo escuro com melhor visibilidade para ambiente externo
    primary = HighVisibilityYellow, // Amarelo de alta visibilidade
    onPrimary = BlackText,
    primaryContainer = Color(0xFF0A3D59), // Versão mais escura do BluePrimary
    onPrimaryContainer = White,
    
    // Cores secundárias mais brilhantes
    secondary = HighContrastBlue,
    onSecondary = White,
    secondaryContainer = Color(0xFF003399), // Azul mais saturado
    onSecondaryContainer = White,
    
    // Terciária mais vibrante
    tertiary = HighVisibilityOrange,
    onTertiary = BlackText,
    
    // Fundo mais escuro com texto muito contrastante
    background = Color(0xFF121212),
    onBackground = White,
    surface = Color(0xFF1E1E1E), // Levemente mais claro que o fundo
    onSurface = White, // Branco puro para máximo contraste
    
    // Variantes de superfície com alto contraste
    surfaceVariant = Color(0xFF323232), // Cinza mais escuro
    onSurfaceVariant = White, // Texto branco para máximo contraste
    
    // Cores de erro mais vibrantes
    error = Color(0xFFFF5252), // Vermelho mais brilhante
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
