package com.js.nowakelock.ui.theme

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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 应用扩展颜色
data class ExtendedColors(
    val accentColor: Color,
    val accentColor2: Color,
    val success: Color,
    val warning: Color,
    val info: Color,
    val divider: Color,
    val caption: Color
)

// 为浅色模式定义扩展颜色
val LightExtendedColors = ExtendedColors(
    accentColor = AccentLight,
    accentColor2 = AccentLight2,
    success = AllowedGreen,
    warning = WarningYellow, 
    info = Primary40,
    divider = Color(0xFFE1E3E5),
    caption = Color(0xFF757575)
)

// 为深色模式定义扩展颜色
val DarkExtendedColors = ExtendedColors(
    accentColor = Color(0xFFFF6E64),
    accentColor2 = Color(0xFFFFD54F),
    success = Color(0xFF66BB6A),
    warning = Color(0xFFFFD54F),
    info = Color(0xFF81D4FA),
    divider = Color(0xFF424242),
    caption = Color(0xFFAAAAAA)
)

// 创建自定义CompositionLocal以提供扩展颜色
val LocalExtendedColors = staticCompositionLocalOf { 
    LightExtendedColors 
}

// 添加扩展颜色访问器
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current

private val DarkColorScheme = darkColorScheme(
    primary = Primary80,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Primary80.copy(alpha = 0.7f),
    onPrimaryContainer = Color(0xFFFFFFFF),
    
    secondary = Secondary80,
    onSecondary = Color(0xFF202124),
    secondaryContainer = Secondary80.copy(alpha = 0.7f),
    onSecondaryContainer = Color(0xFF202124),
    
    tertiary = Tertiary80,
    onTertiary = Color(0xFF202124),
    tertiaryContainer = Tertiary80.copy(alpha = 0.7f),
    onTertiaryContainer = Color(0xFF202124),
    
    background = Color(0xFF202124),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF303134),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF3C4043),
    onSurfaceVariant = Color(0xFFBDC1C6),
    
    error = BlockedRed,
    onError = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary40,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Primary40.copy(alpha = 0.08f),
    onPrimaryContainer = Primary40,
    
    secondary = Secondary40,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Secondary40.copy(alpha = 0.08f),
    onSecondaryContainer = Secondary40,
    
    tertiary = Tertiary40,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Tertiary40.copy(alpha = 0.1f),
    onTertiaryContainer = Tertiary40,
    
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    
    surfaceContainerLowest = Surface,
    surfaceContainerLow = SurfaceContainer,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceVariant,
    
    error = BlockedRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = BlockedRed.copy(alpha = 0.08f),
    onErrorContainer = BlockedRed,
    
    outline = Color(0xFFDADCE0),
    outlineVariant = Color(0xFFDADCE0).copy(alpha = 0.5f)
)

@Composable
fun NoWakeLockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
    
    // 选择扩展颜色
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors
    
    // 使用官方的方式处理系统UI
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}