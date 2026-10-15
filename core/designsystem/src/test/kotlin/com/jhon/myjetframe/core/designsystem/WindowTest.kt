package com.jhon.myjetframe.core.designsystem

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.WindowCompat
import com.jhon.myjetframe.core.designsystem.theme.MyJetFrameTheme
import com.jhon.myjetframe.core.designsystem.theme.Palette
import com.jhon.myjetframe.core.designsystem.theme.ThemeMode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WindowTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun windowEffects_lightTheme_setsLightSystemBars() {
        compose.setContent {
            MyJetFrameTheme(themeMode = ThemeMode.Light, palette = Palette.Line) {
                MyJetFrameWindowEffects()
            }
        }
        compose.waitForIdle()
        val controller = WindowCompat.getInsetsController(
            compose.activity.window,
            compose.activity.window.decorView,
        )
        assertTrue(controller.isAppearanceLightStatusBars)
        assertTrue(controller.isAppearanceLightNavigationBars)
    }
}
