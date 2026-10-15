package com.jhon.myjetframe.core.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jhon.myjetframe.core.designsystem.theme.MyJetFrameTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ButtonTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun button_isDisplayed_andClickable() {
        var clicked = false
        compose.setContent {
            MyJetFrameTheme {
                MyJetFrameButton(
                    onClick = { clicked = true },
                    label = { Text("Go") },
                    icon = {},
                )
            }
        }
        compose.onNodeWithText("Go").assertIsDisplayed().performClick()
        assertTrue(clicked)
    }
}
