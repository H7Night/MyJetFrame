package com.jhon.myjetframe.core.designsystem.theme

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ThemeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun theme_light_publishesNonDarkLineScheme() {
        var captured: MyJetFrameColors? = null
        compose.setContent {
            MyJetFrameTheme(themeMode = ThemeMode.Light, palette = Palette.Line) {
                captured = LocalMyJetFrameColors.current
                Text("x")
            }
        }
        compose.waitForIdle()
        val colors = requireNotNull(captured)
        assertEquals(false, colors.isDark)
        assertEquals(LineLightColorScheme.primary, colors.colorScheme.primary)
    }

    @Test
    fun theme_dark_publishesDarkScheme() {
        var captured: MyJetFrameColors? = null
        compose.setContent {
            MyJetFrameTheme(themeMode = ThemeMode.Dark, palette = Palette.Line) {
                captured = LocalMyJetFrameColors.current
            }
        }
        compose.waitForIdle()
        val colors = requireNotNull(captured)
        assertTrue(colors.isDark)
        assertEquals(LineDarkColorScheme.primary, colors.colorScheme.primary)
    }
}
