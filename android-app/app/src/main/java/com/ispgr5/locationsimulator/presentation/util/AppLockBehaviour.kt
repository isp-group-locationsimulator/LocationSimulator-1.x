package com.ispgr5.locationsimulator.presentation.util

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.ui.graphics.vector.ImageVector
import com.ispgr5.locationsimulator.R

private const val TAG = "AppLock"

enum class AppLockBehaviour(
    @StringRes val labelStringRes: Int,
    @StringRes val explanationStringRes: Int,
    val icon: ImageVector
) {
    NORMAL_BEHAVIOUR(
        labelStringRes = R.string.normal,
        explanationStringRes = R.string.app_lock_behaviour_normal,
        icon = Icons.Default.Lock
    ),
    ALWAYS_SHOW_WHEN_LOCKED(
        labelStringRes = R.string.always,
        explanationStringRes = R.string.app_lock_behaviour_always_show,
        icon = Icons.Default.LockOpen
    ),
    ONLY_SHOW_LOCKED_WHEN_RUNNING(
        labelStringRes = R.string.when_running,
        explanationStringRes = R.string.app_lock_behaviour_only_when_running,
        icon = Icons.Default.LockClock
    )
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