package com.jhon.myjetframe.core.designsystem.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlin.math.min

enum class ThemeMode { System, Light, Dark }

sealed interface Palette {
    data object Line : Palette
    data object Dynamic : Palette
    data class Brand(val seed: Color) : Palette
}

fun brandColorScheme(seed: Color, dark: Boolean): ColorScheme {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(seed.toArgb(), hsl)
    val h = hsl[0]
    val s = hsl[1]
    fun tone(lightness: Float, sat: Float = s): Color =
        Color(ColorUtils.HSLToColor(floatArrayOf(h, sat, lightness)))
    return if (dark) {
        LineDarkColorScheme.copy(
            primary = tone(0.78f, min(s, 0.45f)),
            onPrimary = tone(0.12f, min(s, 0.25f)),
            primaryContainer = tone(0.30f, min(s, 0.35f)),
            onPrimaryContainer = tone(0.90f, min(s, 0.40f)),
            inversePrimary = tone(0.55f, min(s, 0.45f)),
            secondary = tone(0.70f, min(s, 0.25f)),
            tertiary = tone(0.70f, min(s, 0.25f)),
        )
    } else {
        LineLightColorScheme.copy(
            primary = tone(0.42f, min(s, 0.55f)),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = tone(0.92f, min(s, 0.35f)),
            onPrimaryContainer = tone(0.25f, min(s, 0.45f)),
            inversePrimary = tone(0.72f, min(s, 0.45f)),
            secondary = tone(0.45f, min(s, 0.25f)),
            tertiary = tone(0.45f, min(s, 0.25f)),
        )
    }
}

fun resolveColorScheme(palette: Palette, dark: Boolean, context: Context): ColorScheme =
    when (palette) {
        Palette.Line -> if (dark) LineDarkColorScheme else LineLightColorScheme
        Palette.Dynamic ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (dark) darkColorScheme() else lightColorScheme()
            }
        is Palette.Brand -> brandColorScheme(palette.seed, dark)
    }
