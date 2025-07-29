@file:OptIn(ExperimentalMaterial3Api::class)

package com.ispgr5.locationsimulator.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.os.PowerManager
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ispgr5.locationsimulator.R
import com.ispgr5.locationsimulator.data.storageManager.ConfigurationStorageManager
import com.ispgr5.locationsimulator.data.storageManager.SoundStorageManager
import com.ispgr5.locationsimulator.domain.model.ConfigComponent
import com.ispgr5.locationsimulator.domain.model.ConfigurationComponentRoomConverter
import com.ispgr5.locationsimulator.domain.useCase.ConfigurationUseCases
import com.ispgr5.locationsimulator.presentation.add.AddScreen
import com.ispgr5.locationsimulator.presentation.delay.DelayScreen
import com.ispgr5.locationsimulator.presentation.editTimeline.EditTimelineScreen
import com.ispgr5.locationsimulator.presentation.homescreen.HomeScreenScreen
import com.ispgr5.locationsimulator.presentation.homescreen.InfoScreen
import com.ispgr5.locationsimulator.presentation.run.RunScreen
import com.ispgr5.locationsimulator.presentation.run.ServiceIntentKeys
import com.ispgr5.locationsimulator.presentation.run.SimulationService
import com.ispgr5.locationsimulator.presentation.select.SelectScreen
import com.ispgr5.locationsimulator.presentation.settings.SettingsScreen
import com.ispgr5.locationsimulator.presentation.settings.SettingsState
import com.ispgr5.locationsimulator.presentation.sound.SoundDialog
import com.ispgr5.locationsimulator.presentation.sound.SoundScreen
import com.ispgr5.locationsimulator.presentation.universalComponents.SnackbarContent
import com.ispgr5.locationsimulator.presentation.util.Screen
import com.ispgr5.locationsimulator.ui.theme.LocationSimulatorTheme
import com.ispgr5.locationsimulator.ui.theme.ThemeState
import com.ispgr5.locationsimulator.ui.theme.ThemeType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.FileOutputStream
import javax.inject.Inject
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import com.ispgr5.locationsimulator.presentation.util.AppLockBehaviour
import com.ispgr5.locationsimulator.presentation.util.PreferencesKeys
import com.ispgr5.locationsimulator.presentation.util.enableShowAppWhenLocked
import com.ispgr5.locationsimulator.presentation.util.getAppPreferences

val LocalThemeState = compositionLocalOf {
    ThemeState(themeType = ThemeType.AUTO)
}

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // With this soundStorageManager we can access the filesystem wherever we want
    private lateinit var soundStorageManager: SoundStorageManager
    private lateinit var configurationStorageManager: ConfigurationStorageManager
    private var popUpState = mutableStateOf(false)
    private var recordedAudioUri: Uri? = null

    private val snackbarContent: MutableState<SnackbarContent?> = mutableStateOf(null)

    @Inject
    lateinit var configurationUseCases: ConfigurationUseCases

    private val recordAudioIntent = registerForActivityResult(RecordAudioContract()) {
        popUpState.value = true
        recordedAudioUri = it
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundStorageManager = SoundStorageManager(this@MainActivity)
        MainScope().launch {
            installFilesOnFirstStartup(getAppPreferences())
        }
        configurationStorageManager = ConfigurationStorageManager(
            mainActivity = this,
            soundStorageManager = soundStorageManager,
            context = this,
            snackbarContent = snackbarContent,
            configurationUseCases = configurationUseCases
        )
        val storedThemeType =
            getAppPreferences().getString(PreferencesKeys.THEME_TYPE.key, ThemeType.LIGHT.name)
                ?.let {
                    ThemeType.valueOf(it)
                } ?: ThemeType.LIGHT
        val storedDynamicColors =
            getAppPreferences().getBoolean(PreferencesKeys.DYNAMIC_COLORS.key, false)

        val storedAppLockBehaviour = getAppPreferences().getString(
            PreferencesKeys.ALLOW_SHOW_WHEN_LOCKED.key,
            AppLockBehaviour.NORMAL_BEHAVIOUR.name
        )!!.let {
            AppLockBehaviour.valueOf(it)
        }

        val appLockBehaviour = mutableStateOf(storedAppLockBehaviour)

        val firstStart = getAppPreferences().getBoolean(PreferencesKeys.FIRST_START.key, true)

        enableShowAppWhenLocked(appLockBehaviour.value, false)

        setContent {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )
            var showBottomSheet by remember { mutableStateOf(firstStart) }
            val themeState = remember {
                mutableStateOf(
                    ThemeState(themeType = storedThemeType, useDynamicColor = storedDynamicColors)
                )
            }
            var localeList by remember {
                mutableStateOf(AppCompatDelegate.getApplicationLocales())
            }
            Log.d(TAG, localeList.toLanguageTags())
            CompositionLocalProvider(LocalThemeState provides themeState.value) {
                LocationSimulatorTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(), color = colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        val context = LocalContext.current
                        val powerManager by remember {
                            mutableStateOf(context.getSystemService(POWER_SERVICE) as PowerManager)
                        }
                        HandleIncomingIntent(intent)
                        NavigationAppHost(
                            navController = navController,
                            themeState = themeState,
                            snackbarContent = snackbarContent,
                            powerManager = powerManager,
                            appLockBehaviour = appLockBehaviour,
                            sheetState = sheetState,
                            showBottomSheet = showBottomSheet,
                            onToggleSheet = { showBottomSheet = it },
                            currentLocaleList = localeList,
                            setLocaleFromString = {
                                val newLocaleList = LocaleListCompat.forLanguageTags(it)
                                localeList = newLocaleList
                                AppCompatDelegate.setApplicationLocales(newLocaleList)
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun HandleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action in listOf(Intent.ACTION_SEND, Intent.ACTION_VIEW)) {
            LaunchedEffect(key1 = intent) {
                val newConfigurationName =
                    configurationStorageManager.handleImportFromIntent(intent)
                if (newConfigurationName != null) {
                    val feedbackMessage =
                        getString(R.string.success_reading_configuration_name).format(
                            newConfigurationName
                        )
                    snackbarContent.value = SnackbarContent(feedbackMessage, SnackbarDuration.Short)
                }
            }
        }
    }


    /**
     * create Navigation App Host controller, which is responsible for all navigation
     */
    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun NavigationAppHost(
        navController: NavHostController,
        themeState: MutableState<ThemeState>,
        snackbarContent: MutableState<SnackbarContent?>,
        powerManager: PowerManager,
        appLockBehaviour: MutableState<AppLockBehaviour>,
        sheetState: SheetState,
        showBottomSheet: Boolean,
        onToggleSheet: (Boolean) -> Unit,
        currentLocaleList: LocaleListCompat,
        setLocaleFromString: (String) -> Unit
    ) {
        val context = LocalContext.current
        val snackbarHostState = remember {
            SnackbarHostState()
        }
        NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
            composable(Screen.HomeScreen.route) {
                enableShowAppWhenLocked(appLockBehaviour.value, false)
                HomeScreenScreen(
                    navController = navController,
                    checkBatteryOptimizationStatus = {
                        when {
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> true
                            else -> powerManager.isIgnoringBatteryOptimizations(context.packageName)
                        }
                    },
                    batteryOptDisableFunction = { disableBatteryOptimization(powerManager) },
                    soundStorageManager = soundStorageManager,
                    activity = this@MainActivity,
                    appTheme = themeState,
                    snackbarHostState = snackbarHostState,
                    snackbarContent = snackbarContent,
                    appLockBehaviour = appLockBehaviour,
                    sheetState = sheetState,
                    onToggleSheet = onToggleSheet,
                    showBottomSheet = showBottomSheet,
                    currentLocaleList = currentLocaleList,
                    setLocaleFromString = setLocaleFromString
                )
            }
            composable(Screen.InfoScreen.route) {
                enableShowAppWhenLocked(appLockBehaviour.value, false)
                InfoScreen(navController = navController)
            }
            composable(route = Screen.SelectScreen.route) {
                enableShowAppWhenLocked(appLockBehaviour.value, false)
                SelectScreen(
                    navController = navController,
                    configurationStorageManager = configurationStorageManager,
                    soundStorageManager = soundStorageManager,
                    snackbarHostState = snackbarHostState,
                    snackbarContent = snackbarContent
                )
            }
            composable(Screen.AddScreen.route) {
                enableShowAppWhenLocked(appLockBehaviour.value, false)
                AddScreen(
                    navController = navController,
                    configurationStorageManager = configurationStorageManager,
                    getDefaultValuesFunction = getDefaultValues
                )
            }
            composable(Screen.SettingsScreen.route) {
                enableShowAppWhenLocked(appLockBehaviour.value, false)
                SettingsScreen(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    saveDefaultValuesFunction = saveDefaultValues,
                    getDefaultValuesFunction = getDefaultValues
                )
            }
            composable(
                route = Screen.DelayScreen.route,
                arguments = listOf(NavigationArguments.configurationId)
            ) {
                enableShowAppWhenLocked(appLockBehaviour.value, true)
                DelayScreen(
                    navController = navController,
                    startServiceFunction = startService,
                    soundsDirUri = this@MainActivity.filesDir.toString() + "/Sounds/",
                )
            }
            composable(Screen.RunScreen.route) {
                enableShowAppWhenLocked(appLockBehaviour.value, true)
                RunScreen(
                    navController = navController,
                    stopServiceFunction = { stopService() },
                    snackbarHostState = snackbarHostState
                )
            }
            composable(Screen.StopService.route) {
                enableShowAppWhenLocked(appLockBehaviour.value, false)
                navController.navigateUp()
            }
            composable(
                Screen.EditTimelineScreen.route,
                arguments = NavigationArguments.allNavArguments
            ) {
                enableShowAppWhenLocked(appLockBehaviour.value, false)
                EditTimelineScreen(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    getDefaultValuesFunction = getDefaultValues,
                )
            }
            composable(
                Screen.SoundScreen.route, arguments = listOf(NavigationArguments.configurationId)
            ) {
                enableShowAppWhenLocked(appLockBehaviour.value, false)
                SoundScreen(
                    navController = navController,
                    soundStorageManager = soundStorageManager,
                    soundsDirUri = this@MainActivity.filesDir.toString() + "/Sounds/",
                    recordAudio = { recordAudio() },
                    getDefaultValuesFunction = getDefaultValues,
                )
                SoundDialog(
                    popUpState = popUpState
                ) { fileName ->
                    saveAudioFile(fileName)
                    popUpState.value = false
                }
            }
        }
    }


    /**
     * Starts the background service, which plays the audio and vibration
     */
    @OptIn(ExperimentalSerializationApi::class)
    val startService: (String, List<ConfigComponent>, Boolean) -> Unit =
        fun(patternName: String, config: List<ConfigComponent>, randomOrderPlayback: Boolean) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val intent = Intent(this, SimulationService::class.java).apply {
                action = "START"
                putExtra(
                    ServiceIntentKeys.CONFIG_JSON_STRING,
                    ConfigurationComponentRoomConverter().componentListToString(config)
                )
                putExtra(ServiceIntentKeys.PATTERN_NAME_STRING, patternName)
                putExtra(ServiceIntentKeys.SOUNDS_DIR_STRING, "$filesDir/Sounds/")
                putExtra(
                    ServiceIntentKeys.RANDOM_ORDER_PLAYBACK_BOOLEAN,
                    randomOrderPlayback.toString()
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

    /**
     * Stops the background service
     */
    private fun stopService() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Intent(this, SimulationService::class.java).also {
            it.action = "STOP"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
            } else {
                startService(it)
            }
        }
    }

    @SuppressLint("BatteryLife") // We need to have the Service run in the background as long as the user wants. The app only runs, when the user explicitly hits start.
    private fun disableBatteryOptimization(powerManager: PowerManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) return
        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = "package:$packageName".toUri()
        startActivity(intent)
    }

    /**
     * Creates an intent to open the default recording app
     */
    private fun recordAudio() {
        recordAudioIntent.launch(Unit)
    }

    private class RecordAudioContract : ActivityResultContract<Unit, Uri?>() {
        override fun createIntent(context: Context, input: Unit): Intent {
            val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
            val uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            return Intent.createChooser(
                intent, context.getString(R.string.choose_an_audio_recorder_app)
            )
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            if (resultCode != RESULT_OK) {
                return null
            }
            return intent?.data
        }

    }

    /**
     * Saves the recorded audio to the internal filesystem of the app
     */
    private fun saveAudioFile(fileName: String) {
        val inputStream = recordedAudioUri?.let { contentResolver.openInputStream(it) }
        val file = soundStorageManager.getFileInSoundsDir(fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
    }

    /**
     * This function installs the audio files that come with the app.
     */
    private suspend fun installFilesOnFirstStartup(preferences: SharedPreferences) {
        val firstStart: Boolean = preferences.getBoolean(PreferencesKeys.FIRST_START.key, true)
        if (!firstStart) return
        assets.list("sounds")?.forEach { soundName ->
            val extension = MimeTypeMap.getFileExtensionFromUrl(soundName)
                ?: return@forEach // Those ifs shall catch files that we didn't put into assets ourself. Will fail, if there somehow are audio files not from us.
            val type =
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: return@forEach
            val isTypeAudio = type.startsWith("audio")
            if (isTypeAudio) {
                soundStorageManager.addSoundFile(soundName, assets)
            }
        }
        configurationStorageManager.addDefaultConfiguration(
            context = this,
            defaultSettings = getDefaultValues()
        )
        preferences.edit {
            putBoolean(PreferencesKeys.FIRST_START.key, false)
        }
    }

    /**
     * function which saves the set default values for Config Components
     * (in Main Activity because it needs the context)
     */
    private val saveDefaultValues: (state: State<SettingsState>) -> Unit =
        fun(state: State<SettingsState>) {
            val preferences: SharedPreferences = getAppPreferences()
            preferences.edit {
                putInt(PreferencesKeys.MIN_PAUSE_SOUND.key, state.value.minPauseSound)
                putInt(PreferencesKeys.MAX_PAUSE_SOUND.key, state.value.maxPauseSound)
                putFloat(PreferencesKeys.MIN_VOL_SOUND.key, state.value.minVolumeSound)
                putFloat(PreferencesKeys.MAX_VOL_SOUND.key, state.value.maxVolumeSound)
                putInt(PreferencesKeys.MIN_PAUSE_VIB.key, state.value.minPauseVibration)
                putInt(PreferencesKeys.MAX_PAUSE_VIB.key, state.value.maxPauseVibration)
                putInt(PreferencesKeys.MIN_STRENGTH_VIB.key, state.value.minStrengthVibration)
                putInt(PreferencesKeys.MAX_STRENGTH_VIB.key, state.value.maxStrengthVibration)
                putInt(PreferencesKeys.MIN_DURATION_VIB.key, state.value.minDurationVibration)
                putInt(PreferencesKeys.MAX_DURATION_VIB.key, state.value.maxDurationVibration)
                putString(
                    PreferencesKeys.DEFAULT_NAME_VIB.key, state.value.defaultNameVibration
                )
            }
        }

    /**
     * function which returns the set default values for Config Componments
     * (in Main Activity because it needs the context)
     */
    private val getDefaultValues: () -> SettingsState = fun(): SettingsState {
        val preferences: SharedPreferences = getAppPreferences()
        val startDefaultName = "Vibration"
        return SettingsState(
            minPauseSound = preferences.getInt(PreferencesKeys.MIN_PAUSE_SOUND.key, 0),
            maxPauseSound = preferences.getInt(PreferencesKeys.MAX_PAUSE_SOUND.key, 5000),
            minVolumeSound = preferences.getFloat(PreferencesKeys.MIN_VOL_SOUND.key, 0f),
            maxVolumeSound = preferences.getFloat(PreferencesKeys.MAX_VOL_SOUND.key, 1f),

            minPauseVibration = preferences.getInt(PreferencesKeys.MIN_PAUSE_VIB.key, 0),
            maxPauseVibration = preferences.getInt(PreferencesKeys.MAX_PAUSE_VIB.key, 5000),
            minStrengthVibration = preferences.getInt(PreferencesKeys.MIN_STRENGTH_VIB.key, 3),
            maxStrengthVibration = preferences.getInt(
                PreferencesKeys.MAX_STRENGTH_VIB.key, 255
            ),
            minDurationVibration = preferences.getInt(
                PreferencesKeys.MIN_DURATION_VIB.key, 100
            ),
            maxDurationVibration = preferences.getInt(
                PreferencesKeys.MAX_DURATION_VIB.key, 1000
            ),
            defaultNameVibration = preferences.getString(
                PreferencesKeys.DEFAULT_NAME_VIB.key, startDefaultName
            ).toString()
        )
    }
}


object NavigationArguments {
    val configurationId = navArgument(
        name = "configurationId"
    ) {
        type = NavType.IntType
        defaultValue = -1
    }

    private val soundNameToAdd = navArgument(name = "soundNameToAdd") {
        type = NavType.StringType
        defaultValue = ""
    }

    private val minVolume = navArgument(name = "minVolume") {
        type = NavType.FloatType
        defaultValue = 0f
    }

    private val maxVolume = navArgument(name = "maxVolume") {
        type = NavType.FloatType
        defaultValue = 1f
    }

    val minPause = navArgument(name = "minPause") {
        type = NavType.IntType
        defaultValue = 0
    }

    val maxPause = navArgument(name = "maxPause") {
        type = NavType.IntType
        defaultValue = 1000
    }

    val allNavArguments = listOf(
        configurationId,
        soundNameToAdd,
        minVolume,
        maxVolume,
        minPause,
        maxPause
    )
}