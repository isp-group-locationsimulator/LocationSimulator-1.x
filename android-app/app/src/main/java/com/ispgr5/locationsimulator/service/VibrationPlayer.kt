package com.ispgr5.locationsimulator.service

import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.annotation.RequiresApi
private const val TAG = "VibrationPlayer"

class VibrationPlayer(context: Context) {

    private val vibrator by lazy {
        buildVibratorInstance(context)
    }

    private fun buildVibratorInstance(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            Log.d(TAG, "Vibration manager IDs: ${vibratorManager.vibratorIds.joinToString()}")
            vibratorManager.defaultVibrator.also {
                Log.d(TAG, "Using vibrator with ID ${it.id}")
            }
        } else {
            @Suppress("DEPRECATION") // Needed for the support of older Android versions.
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun playVibrationEffect(effect: EffectParameters.Vibration) {
        if (Build.VERSION.SDK_INT >= 26) {
            playEffectBasedVibration(effect)
        }
        else {
            playLegacyVibration(effect)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun playEffectBasedVibration(effect: EffectParameters.Vibration) {
        val (strengthToRequest, forced) = when(vibrator.hasAmplitudeControl()) {
            true -> {
                if (effect.strength == 0) {
                    return // no vibration should be issued, that would request a value that's out of bound from the system
                }
                effect.strength to ""
            }
            else -> 255 to " (forced)"
        }
        Log.d(
            TAG,
            "Creating Vibration... Duration: ${effect.durationMillis} ms, Strength: $strengthToRequest$forced"
        )
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                effect.durationMillis,
                strengthToRequest
            )
        )
    }

    private fun playLegacyVibration(effect: EffectParameters.Vibration) {
        Log.d(TAG, "Creating Vibration... Duration: ${effect.durationMillis} ms")
        @Suppress("DEPRECATION") // Needed for the support of older Android versions.
        vibrator.vibrate(effect.durationMillis)
    }

    fun cancel() {
        vibrator.cancel()
    }

}