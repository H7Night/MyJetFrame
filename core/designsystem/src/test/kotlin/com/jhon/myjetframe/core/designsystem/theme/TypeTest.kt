package com.jhon.myjetframe.core.designsystem.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeTest {
    @Test
    fun header_is28sp() {
        assertEquals(28.sp, Typography.header().fontSize)
    }

    @Test
    fun watermark_is40sp() {
        assertEquals(40.sp, Typography.watermark().fontSize)
    }

    @Test
    fun displayLarge_isBold() {
        assertEquals(FontWeight.Bold, Typography.displayLarge.fontWeight)
    }

    @Test
    fun numbers_usesMonoFamily() {
        val style = Typography.bodyLarge.numbers()
        assertEquals(Typography.bodyLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Numbers).fontFamily, style.fontFamily)
    }
}
