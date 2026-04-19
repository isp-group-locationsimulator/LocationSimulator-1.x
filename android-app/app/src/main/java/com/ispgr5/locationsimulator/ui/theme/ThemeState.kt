package com.ispgr5.locationsimulator.ui.theme

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ispgr5.locationsimulator.R

/**
 * The Theme State to control whether the Dark Mode is On or not
 */
data class ThemeState (
    val themeType: ThemeType = ThemeType.LIGHT, //whether the dark mode is on or not (Light Mode is standard)
    val useDynamicColor: Boolean = false
)

enum class ThemeType(
    @param:StringRes val labelStringRes: Int,
    @param:DrawableRes val icon: Int
) {
    LIGHT(R.string.light, R.drawable.light_mode_24px),
    AUTO(R.string.auto, R.drawable.brightness_auto_24px),
    DARK(R.string.dark, R.drawable.dark_mode_24px)
}