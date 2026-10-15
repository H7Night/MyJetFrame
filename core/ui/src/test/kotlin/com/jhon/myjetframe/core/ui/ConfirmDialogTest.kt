package com.jhon.myjetframe.core.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jhon.myjetframe.core.designsystem.theme.MyJetFrameTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConfirmDialogTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun confirmDialog_showsText_andConfirms() {
        var confirmed = false
        compose.setContent {
            MyJetFrameTheme {
                ConfirmDialog(
                    title = "Delete?",
                    message = "This cannot be undone.",
                    confirmText = "OK",
                    dismissText = "Cancel",
                    onConfirm = { confirmed = true },
                    onDismiss = {},
                )
            }
        }
        compose.onNodeWithText("Delete?").assertIsDisplayed()
        compose.onAllNodesWithText("OK")[0].performClick()
        assertTrue(confirmed)
    }
}
