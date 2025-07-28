package com.ispgr5.locationsimulator.presentation.util

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.annotation.StringRes
import com.ispgr5.locationsimulator.R

private const val TAG = "AppLock"

enum class AppLockBehaviour(@StringRes val labelStringRes: Int) {
    NORMAL_BEHAVIOUR(R.string.normal),
    ALWAYS_SHOW_WHEN_LOCKED(R.string.always),
    ONLY_SHOW_LOCKED_WHEN_RUNNING(R.string.when_running)
}

fun Activity.enableShowAppWhenLocked(
    userSetting: AppLockBehaviour,
    newValueIfOnlyWhenRunning: Boolean
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return
    when (userSetting) {
        AppLockBehaviour.NORMAL_BEHAVIOUR -> setShowWhenLocked(false)
        else -> {
            val newValue =
                userSetting == AppLockBehaviour.ALWAYS_SHOW_WHEN_LOCKED || newValueIfOnlyWhenRunning
            Log.d(TAG, "Set app lock to $newValue with policy $userSetting")
            setShowWhenLocked(newValue)
        }
    }
}