package com.jhon.myjetframe.core.designsystem

import androidx.compose.ui.test.junit4.createComposeRule
import com.jhon.myjetframe.core.designsystem.theme.MyJetFrameTheme
import com.jhon.myjetframe.core.designsystem.theme.Palette
import com.jhon.myjetframe.core.designsystem.theme.ThemeMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WindowTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun windowEffects_appliesWithoutCrashing() {
        compose.setContent {
            MyJetFrameTheme(themeMode = ThemeMode.Light, palette = Palette.Line) {
                MyJetFrameWindowEffects()
            }
        }
        compose.waitForIdle()
        // Guarded with `as? Activity`; no crash on any host.
    }
}
