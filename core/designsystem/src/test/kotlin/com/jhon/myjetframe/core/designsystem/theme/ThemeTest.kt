package com.jhon.myjetframe.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ThemeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun theme_rendersContent_withLinePalette() {
        compose.setContent {
            MyJetFrameTheme(themeMode = ThemeMode.Light, palette = Palette.Line) {
                Text(MaterialTheme.colorScheme.primary.toString())
            }
        }
        compose.waitForIdle()
        // No crash and the tree composed successfully.
    }
}
