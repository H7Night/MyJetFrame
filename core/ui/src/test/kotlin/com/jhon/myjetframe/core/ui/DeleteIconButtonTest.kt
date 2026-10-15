package com.jhon.myjetframe.core.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.jhon.myjetframe.core.designsystem.theme.MyJetFrameTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeleteIconButtonTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun deleteIconButton_exposesContentDescription() {
        compose.setContent {
            MyJetFrameTheme {
                DeleteIconButton(onClick = {}, contentDescription = "Delete")
            }
        }
        compose.onNodeWithContentDescription("Delete").assertExists()
    }
}
