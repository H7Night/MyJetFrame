package com.jhon.myjetframe.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class PaletteTest {
    private fun hue(c: Color): Float {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(c.toArgb(), hsl)
        return hsl[0]
    }

    @Test
    fun line_resolvesToLineSchemes() {
        val ctx = RuntimeEnvironment.getApplication()
        assertEquals(LineLightColorScheme, resolveColorScheme(Palette.Line, dark = false, context = ctx))
        assertEquals(LineDarkColorScheme, resolveColorScheme(Palette.Line, dark = true, context = ctx))
    }

    @Test
    fun brand_primaryHueMatchesSeed() {
        val seed = Color(0xFF00695B)
        val scheme = brandColorScheme(seed, dark = false)
        assertTrue(kotlin.math.abs(hue(scheme.primary) - hue(seed)) < 20f)
    }

    @Test
    fun brand_extremeSeed_keepsReadableContrast() {
        val scheme = brandColorScheme(Color.White, dark = false)
        val ratio = ColorUtils.calculateContrast(
            scheme.onPrimary.toArgb(),
            scheme.primary.toArgb(),
        )
        assertTrue("contrast ratio=$ratio", ratio >= 3.0)
    }

    @Test
    @Config(sdk = [30])
    fun dynamic_belowApi31_usesBaselineScheme() {
        val ctx = RuntimeEnvironment.getApplication()
        assertEquals(
            lightColorScheme().toString(),
            resolveColorScheme(Palette.Dynamic, dark = false, context = ctx).toString(),
        )
        assertEquals(
            darkColorScheme().toString(),
            resolveColorScheme(Palette.Dynamic, dark = true, context = ctx).toString(),
        )
    }
}
