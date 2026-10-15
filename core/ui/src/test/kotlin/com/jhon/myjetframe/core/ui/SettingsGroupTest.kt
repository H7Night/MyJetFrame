package com.jhon.myjetframe.core.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.jhon.myjetframe.core.designsystem.theme.MyJetFrameTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsGroupTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun preferenceSwitchRow_showsTitle() {
        compose.setContent {
            MyJetFrameTheme {
                PreferenceSwitchRow(title = "Dark mode", checked = true, onCheckedChange = {})
            }
        }
        compose.onNodeWithText("Dark mode").assertIsDisplayed()
    }
}
