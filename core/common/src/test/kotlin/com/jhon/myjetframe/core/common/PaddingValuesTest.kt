package com.jhon.myjetframe.core.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class PaddingValuesTest {
    @Test
    fun plus_addsEachEdge() {
        val a = PaddingValues(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 8.dp)
        val b = PaddingValues(start = 2.dp, top = 1.dp, end = 3.dp, bottom = 5.dp)
        val result = a + b
        assertEquals(6.dp, result.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr))
        assertEquals(9.dp, result.calculateTopPadding())
        assertEquals(7.dp, result.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr))
        assertEquals(13.dp, result.calculateBottomPadding())
    }

    @Test
    fun minus_subtractsEachEdge() {
        val a = PaddingValues(start = 6.dp, top = 9.dp, end = 7.dp, bottom = 13.dp)
        val b = PaddingValues(start = 2.dp, top = 1.dp, end = 3.dp, bottom = 5.dp)
        val result = a - b
        assertEquals(4.dp, result.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr))
        assertEquals(8.dp, result.calculateTopPadding())
        assertEquals(4.dp, result.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr))
        assertEquals(8.dp, result.calculateBottomPadding())
    }
}
