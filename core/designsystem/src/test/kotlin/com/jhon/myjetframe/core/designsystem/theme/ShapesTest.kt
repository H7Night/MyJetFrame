package com.jhon.myjetframe.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class ShapesTest {
    @Test
    fun shapes_matchTokenScale() {
        assertEquals(RoundedCornerShape(6.dp), Shapes.extraSmall)
        assertEquals(RoundedCornerShape(8.dp), Shapes.small)
        assertEquals(RoundedCornerShape(10.dp), Shapes.medium)
        assertEquals(RoundedCornerShape(12.dp), Shapes.large)
        assertEquals(RoundedCornerShape(14.dp), Shapes.extraLarge)
    }

    @Test
    fun borderWidths_areHairline() {
        assertEquals(1.dp, MyJetFrameBorderWidth)
        assertEquals(0.8.dp, MyJetFrameHairlineWidth)
    }
}
