package com.jhon.myjetframe.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

data class MyJetFrameColors(val isDark: Boolean, val colorScheme: ColorScheme)

val LocalMyJetFrameColors: ProvidableCompositionLocal<MyJetFrameColors> =
    staticCompositionLocalOf { MyJetFrameColors(false, LineLightColorScheme) }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyJetFrameTheme(
    themeMode: ThemeMode = ThemeMode.System,
    palette: Palette = Palette.Line,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = remember(themeMode, systemDark) {
        when (themeMode) {
            ThemeMode.System -> systemDark
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }
    }
    val context = LocalContext.current
    val colorScheme = remember(palette, isDark, context) {
        resolveColorScheme(palette, isDark, context)
    }
    CompositionLocalProvider(
        LocalMyJetFrameColors provides MyJetFrameColors(isDark, colorScheme),
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}
