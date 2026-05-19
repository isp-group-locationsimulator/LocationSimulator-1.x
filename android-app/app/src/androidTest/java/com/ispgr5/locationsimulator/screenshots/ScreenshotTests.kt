package com.ispgr5.locationsimulator.screenshots

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.intl.LocaleList
import com.ispgr5.locationsimulator.di.AppModule
import com.ispgr5.locationsimulator.presentation.LocalThemeState
import com.ispgr5.locationsimulator.presentation.add.AddScreenPreview
import com.ispgr5.locationsimulator.presentation.delay.DelayScreenPreview
import com.ispgr5.locationsimulator.presentation.editTimeline.EditTimelineDialogShownPreview
import com.ispgr5.locationsimulator.presentation.editTimeline.EditTimelineNormalPreview
import com.ispgr5.locationsimulator.presentation.editTimeline.EditTimelineUnsupportedIntensityPreview
import com.ispgr5.locationsimulator.presentation.homescreen.HomeScreenPreview
import com.ispgr5.locationsimulator.presentation.homescreen.InfoScreenPreview
import com.ispgr5.locationsimulator.presentation.run.RunScreenActivePreview
import com.ispgr5.locationsimulator.presentation.run.RunScreenPausedPreview
import com.ispgr5.locationsimulator.presentation.select.SelectScreenDeleteModePreview
import com.ispgr5.locationsimulator.presentation.select.SelectScreenNormalPreview
import com.ispgr5.locationsimulator.presentation.settings.SettingsScreenSoundPreview
import com.ispgr5.locationsimulator.presentation.settings.SettingsScreenVibrationPreview
import com.ispgr5.locationsimulator.presentation.sound.SoundScreenForDeletionPreview
import com.ispgr5.locationsimulator.presentation.sound.SoundScreenPlayingPreview
import com.ispgr5.locationsimulator.presentation.sound.SoundScreenStoppedPreview
import com.ispgr5.locationsimulator.ui.theme.LocationSimulatorTheme
import com.ispgr5.locationsimulator.ui.theme.ThemeState
import com.ispgr5.locationsimulator.ui.theme.ThemeType
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import java.io.File
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.test.onRoot


data class ScreenshotScope(
    val screenshotName: String, val theme: ThemeState
)


private const val TAG = "ScreenshotTests"

@HiltAndroidTest
@UninstallModules(AppModule::class)
class ScreenshotTests {

    @get:Rule(order = 0)
    val hiltAndroidRule by lazy {
        HiltAndroidRule(this)
    }

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun init() {
        hiltAndroidRule.inject()
    }

    val screenshotList = mapOf(
        "add" to @Composable { AddScreenPreview() },
        "delay" to @Composable { DelayScreenPreview() },
        "edit_dialog" to @Composable { EditTimelineDialogShownPreview() },
        "edit_normal" to @Composable { EditTimelineNormalPreview() },
        "edit_unsupported_intensity" to @Composable { EditTimelineUnsupportedIntensityPreview() },
        "home" to @Composable { HomeScreenPreview() },
        "info" to @Composable { InfoScreenPreview() },
        "run_active" to @Composable { RunScreenActivePreview() },
        "run_paused" to @Composable { RunScreenPausedPreview() },
        "select_delete" to @Composable { SelectScreenDeleteModePreview() },
        "select_normal" to @Composable { SelectScreenNormalPreview() },
        "settings_sound" to @Composable { SettingsScreenSoundPreview() },
        "settings_vibration" to @Composable { SettingsScreenVibrationPreview() },
        "sound_deletion" to @Composable { SoundScreenForDeletionPreview() },
        "sound_playing" to @Composable { SoundScreenPlayingPreview() },
        "sound_stopped" to @Composable { SoundScreenStoppedPreview() },
    )

    @Test
    fun performAllScreenshots() {
        val totalScreenshots = 2 * screenshotList.size
        composeTestRule.mainClock.autoAdvance = false
        var themeMode by mutableStateOf(ThemeState(ThemeType.LIGHT))
        var screenshotIndex by mutableStateOf(0)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalThemeState provides themeMode) {
                LocationSimulatorTheme {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("ROOT"),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        val animatedScreenshotIndex by animateIntAsState(
                            targetValue = screenshotIndex,
                            animationSpec = tween(durationMillis = 250)
                        )
                        val screenshotEntry by remember {
                            derivedStateOf {
                                screenshotList.entries.toList()[animatedScreenshotIndex]
                            }
                        }
                        screenshotEntry.value.invoke()
                    }
                }
            }
        }

        for (themeOption in listOf(ThemeType.LIGHT, ThemeType.DARK)) {
            themeMode = ThemeState(themeOption, useDynamicColor = false)
            for (index in 0 until screenshotList.size) {
                screenshotIndex = index
                composeTestRule.mainClock.advanceTimeBy(2000L)
                val screenshotEntry = screenshotList.entries.toList()[index]
                val screenshotName =
                    "${Locale.getDefault()}_${screenshotEntry.key}_${themeMode.themeType.name}"
                val webpName = screenshotName.plus(".webp")
                val pngName = screenshotName.plus(".png")
                composeTestRule.onNodeWithTag("ROOT").captureToImage().let { bitmap ->
                    File("/sdcard/Documents", webpName).outputStream().use { outStream ->
                        bitmap.asAndroidBitmap()
                            .compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 0, outStream)
                    }
                    File("/sdcard/Documents", pngName).outputStream().use { outputStream ->
                        bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 0, outputStream)
                    }
                    val logIndex =
                        (if (themeOption == ThemeType.DARK) screenshotList.size else 0) + index + 1
                    Log.i(
                        TAG,
                        "$logIndex / ${totalScreenshots}: ${screenshotName}"
                    )
                }
            }
            composeTestRule.mainClock.advanceTimeBy(2000L)
            Thread.sleep(2000L)
        }
    }
}
