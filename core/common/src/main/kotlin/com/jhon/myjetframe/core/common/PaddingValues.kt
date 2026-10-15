/*
 * Copyright (C) 2025 LooKeR & Contributors
 * Copyright (C) 2026 H7Night <h7night@gmail.com>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jhon.myjetframe.core.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection

operator fun PaddingValues.plus(other: PaddingValues): PaddingValues =
    object : PaddingValues {
        override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
            this@plus.calculateLeftPadding(layoutDirection) + other.calculateLeftPadding(layoutDirection)
        override fun calculateTopPadding() = this@plus.calculateTopPadding() + other.calculateTopPadding()
        override fun calculateRightPadding(layoutDirection: LayoutDirection) =
            this@plus.calculateRightPadding(layoutDirection) + other.calculateRightPadding(layoutDirection)
        override fun calculateBottomPadding() = this@plus.calculateBottomPadding() + other.calculateBottomPadding()
    }

operator fun PaddingValues.minus(other: PaddingValues): PaddingValues =
    object : PaddingValues {
        override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
            this@minus.calculateLeftPadding(layoutDirection) - other.calculateLeftPadding(layoutDirection)
        override fun calculateTopPadding() = this@minus.calculateTopPadding() - other.calculateTopPadding()
        override fun calculateRightPadding(layoutDirection: LayoutDirection) =
            this@minus.calculateRightPadding(layoutDirection) - other.calculateRightPadding(layoutDirection)
        override fun calculateBottomPadding() = this@minus.calculateBottomPadding() - other.calculateBottomPadding()
    }
