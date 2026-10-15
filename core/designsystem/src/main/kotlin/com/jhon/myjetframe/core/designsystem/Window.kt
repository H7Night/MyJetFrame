package com.jhon.myjetframe.core.designsystem

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.jhon.myjetframe.core.designsystem.theme.LocalMyJetFrameColors

@Composable
fun MyJetFrameWindowEffects() {
    val view = LocalView.current
    if (view.isInEditMode) return
    val colors = LocalMyJetFrameColors.current
    SideEffect {
        val activity = view.context as? Activity ?: return@SideEffect
        activity.window.setBackgroundDrawable(
            ColorDrawable(colors.colorScheme.surface.toArgb()),
        )
        with(WindowCompat.getInsetsController(activity.window, view)) {
            isAppearanceLightStatusBars = !colors.isDark
            isAppearanceLightNavigationBars = !colors.isDark
        }
    }
}

fun configureMyJetFrameWindow(activity: Activity, isDark: Boolean) {
    with(WindowCompat.getInsetsController(activity.window, activity.window.decorView)) {
        isAppearanceLightStatusBars = !isDark
        isAppearanceLightNavigationBars = !isDark
    }
}
