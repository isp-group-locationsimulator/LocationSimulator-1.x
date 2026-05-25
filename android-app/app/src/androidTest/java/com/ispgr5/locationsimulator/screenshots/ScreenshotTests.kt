package com.ispgr5.locationsimulator.screenshots

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.intl.LocaleList
import androidx.test.core.app.takeScreenshot
import com.ispgr5.locationsimulator.di.AppModule
import com.ispgr5.locationsimulator.presentation.LocalThemeState
import com.ispgr5.locationsimulator.presentation.add.AddScreenPreview
import com.ispgr5.locationsimulator.presentation.delay.DelayScreenPreview
import com.ispgr5.locationsimulator.presentation.editTimeline.EditTimelineDialogShownPreview
import com.ispgr5.locationsimulator.presentation.editTimeline.EditTimelineNormalPreview
import com.ispgr5.locationsimulator.presentation.editTimeline.EditTimelineUnsupportedIntensityPreview
import com.ispgr5.locationsimulator.presentation.homescreen.HomeScreenPreview
import com.ispgr5.locationsimulator.presentation.homescreen.HomeScreenSheetShownPreview
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
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File


data class ScreenshotScope(
    val screenshotName: String, val theme: ThemeState
)


private const val TAG = "ScreenshotTests"

@HiltAndroidTest
@UninstallModules(AppModule::class)
@RunWith(Parameterized::class)
class ScreenshotTest(val name: String, val composable: @Composable () -> Unit) {

    val outputDir = File("/sdcard/Documents/screenshots")

    @get:Rule(order = 0)
    val hiltAndroidRule by lazy {
        HiltAndroidRule(this)
    }

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun init() {
        hiltAndroidRule.inject()
        outputDir.mkdirs()
    }

    private val lightTheme = ThemeState(ThemeType.LIGHT, useDynamicColor = false)
    private val darkTheme = ThemeState(ThemeType.DARK, useDynamicColor = false)

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun parameters() = listOf(
            arrayOf("add", @Composable { AddScreenPreview() }),
            arrayOf("delay", @Composable { DelayScreenPreview() }),
            arrayOf("edit_dialog", @Composable { EditTimelineDialogShownPreview() }),
            arrayOf("edit_normal", @Composable { EditTimelineNormalPreview() }),
            arrayOf("edit_unsupported_intensity", @Composable { EditTimelineUnsupportedIntensityPreview() }),
            arrayOf("home", @Composable { HomeScreenPreview() }),
            arrayOf("home_sheet", @Composable { HomeScreenSheetShownPreview() }),
            arrayOf("info", @Composable { InfoScreenPreview() }),
            arrayOf("run_active", @Composable { RunScreenActivePreview() }),
            arrayOf("run_paused", @Composable { RunScreenPausedPreview() }),
            arrayOf("select_delete", @Composable { SelectScreenDeleteModePreview() }),
            arrayOf("select_normal", @Composable { SelectScreenNormalPreview() }),
            arrayOf("settings_sound", @Composable { SettingsScreenSoundPreview() }),
            arrayOf("settings_vibration", @Composable { SettingsScreenVibrationPreview() }),
            arrayOf("sound_deletion", @Composable { SoundScreenForDeletionPreview() }),
            arrayOf("sound_playing", @Composable { SoundScreenPlayingPreview() }),
            arrayOf("sound_stopped", @Composable { SoundScreenStoppedPreview() }),
        )
    }

    @Test
    fun lightEnglish() {
        renderScreenshot(this.name, lightTheme, "en-US", composable)
    }

    @Test
    fun lightGerman() {
        renderScreenshot(this.name, lightTheme, "de-DE", composable)
    }

    @Test
    fun darkEnglish() {
        renderScreenshot(this.name, darkTheme, "en-US", composable)
    }

    @Test
    fun darkGerman() {
        renderScreenshot(this.name, darkTheme, "de-DE", composable)
    }


//    fun performAllScreenshots(localeString: String) {
//
//        val totalScreenshots = 2 * screenshotList.size
//        composeTestRule.mainClock.autoAdvance = false
//        var themeMode by mutableStateOf(ThemeState(ThemeType.LIGHT))
//        var screenshotIndex by mutableStateOf(0)
//        var screenshotName by mutableStateOf("")
//
//        composeTestRule.setContent {
//            DeviceConfigurationOverride(
//                DeviceConfigurationOverride.Locales(LocaleList(localeString))
//            ) {
//
//        }
//
//        for (themeOption in listOf(ThemeType.LIGHT, ThemeType.DARK)) {
//            themeMode = ThemeState(themeOption, useDynamicColor = false)
//            for (index in 0 until screenshotList.size) {
//                screenshotIndex = index
//                composeTestRule.mainClock.advanceTimeBy(2000L)
//                composeTestRule.waitForIdle()
//                composeTestRule.mainClock.advanceTimeBy(2000L)
//
//                val screenshotEntry = screenshotList.entries.toList()[index]
//                screenshotName = "${localeString}_${screenshotEntry.key}_${themeMode.themeType.name}"
//                val webpName = screenshotName.plus(".webp")
//                val pngName = screenshotName.plus(".png")
//                val outputDir = File("/sdcard/Documents/screenshots").also {
//                    it.mkdirs()
//                }
//
//                val bitmap = takeScreenshot().asImageBitmap().asAndroidBitmap()
//                val webpFile = File(outputDir, webpName).also {
//                    it.delete()
//                }
//                val pngFile = File(outputDir, pngName).also {
//                    it.delete()
//                }
//
//                // Delete existing files to avoid permission issues when overwriting
//                webpFile.delete()
//                pngFile.delete()
//
//                webpFile.outputStream().use { outStream ->
//                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, outStream)
//                }
//                pngFile.outputStream().use { outputStream ->
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//                }
//                val logIndex =
//                    (if (themeOption == ThemeType.DARK) screenshotList.size else 0) + index + 1
//                Log.i(
//                    TAG,
//                    "$logIndex / ${totalScreenshots}: $screenshotName"
//                )
//            }
//            composeTestRule.mainClock.advanceTimeBy(2000L)
//            Thread.sleep(2000L)
//        }
//    }

    @OptIn(ExperimentalTestApi::class)
    fun renderScreenshot(screenshotKey: String, themeState: ThemeState, localeString: String, composable: @Composable () -> Unit) {
//        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.Locales(LocaleList(localeString))) {
                CompositionLocalProvider(LocalThemeState provides themeState) {
                    LocationSimulatorTheme {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("ROOT"),
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            composable()
                        }
                    }
                }
            }
        }
        composeTestRule.waitUntilAtLeastOneExists(isRoot())
        val screenshotName = "${localeString}_${screenshotKey}_${themeState.themeType.name}"
        val webpName = screenshotName.plus(".webp")
        val pngName = screenshotName.plus(".png")
        composeTestRule.waitForIdle()
        val bitmap = takeScreenshot().asImageBitmap().asAndroidBitmap()
        val webpFile = File(outputDir, webpName).also {
            it.delete()
        }
        val pngFile = File(outputDir, pngName).also {
            it.delete()
        }
        webpFile.outputStream().use { outStream ->
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, outStream)
        }
        pngFile.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        Log.i(TAG, "Finished $screenshotName")
    }

}
