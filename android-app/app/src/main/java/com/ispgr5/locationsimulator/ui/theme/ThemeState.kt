package com.ispgr5.locationsimulator.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.ui.graphics.vector.ImageVector
import com.ispgr5.locationsimulator.R

/**
 * The Theme State to control whether the Dark Mode is On or not
 */
data class ThemeState (
    val themeType: ThemeType = ThemeType.LIGHT, //whether the dark mode is on or not (Light Mode is standard)
    val useDynamicColor: Boolean = false
)

enum class ThemeType(val labelStringRes: Int, val icon: ImageVector) {
    LIGHT(R.string.light, Icons.Default.LightMode),
    AUTO(R.string.auto, Icons.Default.BrightnessAuto),
    DARK(R.string.dark, Icons.Default.DarkMode)
}