package com.jhon.myjetframe.core.common

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DpTest {
    @Test
    fun contains_isInclusive_ofBounds() {
        val range = 4.dp..10.dp
        assertTrue(range.contains(4.dp))
        assertTrue(range.contains(10.dp))
        assertTrue(range.contains(7.dp))
    }

    @Test
    fun contains_isFalse_outsideBounds() {
        val range = 4.dp..10.dp
        assertFalse(range.contains(3.dp))
        assertFalse(range.contains(11.dp))
    }
}
