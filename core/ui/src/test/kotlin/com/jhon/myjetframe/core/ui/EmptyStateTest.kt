package com.jhon.myjetframe.core.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.jhon.myjetframe.core.designsystem.icon.MyJetFrameIcons
import com.jhon.myjetframe.core.designsystem.theme.MyJetFrameTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EmptyStateTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun emptyState_showsText() {
        compose.setContent {
            MyJetFrameTheme {
                EmptyState(icon = MyJetFrameIcons.Stack, text = "Nothing here")
            }
        }
        compose.onNodeWithText("Nothing here").assertIsDisplayed()
    }
}
