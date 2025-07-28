package com.ispgr5.locationsimulator.presentation.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.activity.ComponentActivity


fun Activity.getAppPreferences(): SharedPreferences =
    this.getSharedPreferences("prefs", Context.MODE_PRIVATE)!!

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

enum class PreferencesKeys(private val stringKey: String? = null) {
    // optional prop added for backwards compat with old prefs
    MIN_PAUSE_SOUND,
    MAX_PAUSE_SOUND,
    MIN_VOL_SOUND,
    MAX_VOL_SOUND,
    MIN_PAUSE_VIB,
    MAX_PAUSE_VIB,
    MIN_STRENGTH_VIB,
    MAX_STRENGTH_VIB,
    MIN_DURATION_VIB,
    MAX_DURATION_VIB,
    DEFAULT_NAME_VIB,
    THEME_TYPE("themeType"),
    ALLOW_SHOW_WHEN_LOCKED,
    FIRST_START("firstStart"),
    DYNAMIC_COLORS("dynamicColors");

    val key: String
        get() = stringKey ?: name
}