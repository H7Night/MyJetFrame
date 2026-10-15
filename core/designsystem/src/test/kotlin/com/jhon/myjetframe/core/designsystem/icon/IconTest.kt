package com.jhon.myjetframe.core.designsystem.icon

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class IconTest {
    @Test
    fun builder_setsDefaultSize() {
        val v: ImageVector = icon("test") {}
        assertEquals(24.dp, v.defaultWidth)
        assertEquals(24.dp, v.defaultHeight)
    }

    @Test
    fun iconSet_isNonEmpty() {
        val icons: List<ImageVector> = MyJetFrameIcons.all
        org.junit.Assert.assertTrue(icons.isNotEmpty())
    }
}
