@file:OptIn(ExperimentalMaterial3Api::class)

package com.ispgr5.locationsimulator.presentation.homescreen

import android.os.Build
import android.os.LocaleList
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gigamole.composescrollbars.Scrollbars
import com.gigamole.composescrollbars.config.ScrollbarsConfig
import com.gigamole.composescrollbars.config.ScrollbarsOrientation
import com.gigamole.composescrollbars.rememberScrollbarsState
import com.gigamole.composescrollbars.scrolltype.ScrollbarsScrollType
import com.ispgr5.locationsimulator.BuildConfig
import com.ispgr5.locationsimulator.R
import com.ispgr5.locationsimulator.core.util.TestTags
import com.ispgr5.locationsimulator.data.storageManager.SoundStorageManager
import com.ispgr5.locationsimulator.domain.model.Configuration
import com.ispgr5.locationsimulator.presentation.MainActivity
import com.ispgr5.locationsimulator.presentation.previewData.AppPreview
import com.ispgr5.locationsimulator.presentation.previewData.PreviewData
import com.ispgr5.locationsimulator.presentation.universalComponents.LocationSimulatorTopBar
import com.ispgr5.locationsimulator.presentation.universalComponents.SnackbarContent
import com.ispgr5.locationsimulator.presentation.util.AppLockBehaviour
import com.ispgr5.locationsimulator.presentation.util.AppSnackbarHost
import com.ispgr5.locationsimulator.presentation.util.PreferencesKeys
import com.ispgr5.locationsimulator.presentation.util.RenderSnackbarOnChange
import com.ispgr5.locationsimulator.presentation.util.Screen
import com.ispgr5.locationsimulator.presentation.util.getActivity
import com.ispgr5.locationsimulator.presentation.util.getAppPreferences
import com.ispgr5.locationsimulator.ui.theme.LocationSimulatorTheme
import com.ispgr5.locationsimulator.ui.theme.ThemeState

/**
 * The Home Screen.
 *
 */
@ExperimentalAnimationApi
@Composable
fun HomeScreenScreen(
    navController: NavController,
    activity: MainActivity,
    viewModel: HomeScreenViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    appTheme: MutableState<ThemeState>,
    snackbarContent: MutableState<SnackbarContent?>,
    sheetState: SheetState,
    showBottomSheet: Boolean,
    appLockBehaviour: MutableState<AppLockBehaviour>,
    currentLocaleList: LocaleListCompat,
    soundStorageManager: SoundStorageManager,
    checkBatteryOptimizationStatus: () -> Boolean,
    batteryOptDisableFunction: () -> Unit,
    onToggleSheet: (Boolean) -> Unit,
    setLocaleFromString: (String) -> Unit
) {
    viewModel.updateConfigurationWithErrorsState(soundStorageManager = soundStorageManager)
    val state = viewModel.state.value
    val context = LocalContext.current
    RenderSnackbarOnChange(snackbarHostState = snackbarHostState, snackbarContent = snackbarContent)

    HomeScreenScaffold(
        homeScreenState = state,
        appTheme = appTheme,
        snackbarHostState = snackbarHostState,
        sheetState = sheetState,
        showBottomSheet = showBottomSheet,
        appLockBehaviour = appLockBehaviour,
        currentLocaleList = currentLocaleList,
        onInfoClick = {
            navController.navigate(Screen.InfoScreen.route)
        },
        onSelectProfile = {
            viewModel.onEvent(HomeScreenEvent.SelectConfiguration)
            navController.navigate(Screen.SelectScreen.route)
        },
        onSelectFavourite = { configuration ->
            when {
                state.configurationsWithErrors.find { conf -> conf.id == configuration.id } == null -> {
                    navController.navigate(
                        Screen.DelayScreen.createRoute(
                            configuration.id!!
                        )
                    )
                }

                else -> {
                    val errorStrings = viewModel.whatIsHisErrors(
                        configuration, soundStorageManager
                    )
                    val snackbarMessage = when (errorStrings.size) {
                        1 -> context.getString(
                            R.string.error_single_sound_not_found, errorStrings.first()
                        )

                        else -> {
                            val errorText = errorStrings.joinToString(", ") {
                                "'${it}'"
                            }
                            context.getString(
                                R.string.error_multiple_sounds_not_found, errorText
                            )
                        }
                    }
                    snackbarContent.value = SnackbarContent(
                        text = snackbarMessage,
                        snackbarDuration = SnackbarDuration.Indefinite,
                        actionLabel = context.getString(android.R.string.ok),
                    )
                }

            }
        },
        onSelectTheme = { newTheme ->
            appTheme.value = newTheme
            viewModel.onEvent(
                HomeScreenEvent.ChangedAppTheme(
                    activity = activity, themeState = newTheme
                )
            )
        },
        checkBatteryOptimizationStatus = checkBatteryOptimizationStatus,
        onLaunchBatteryOptimizerDisable = {
            viewModel.onEvent(HomeScreenEvent.DisableBatteryOptimization {
                batteryOptDisableFunction()
            })
        },
        onSelectAppLockBehaviour = {
            context.getActivity()?.getAppPreferences()?.edit {
                putString(PreferencesKeys.ALLOW_SHOW_WHEN_LOCKED.key, it.name)
            }
            appLockBehaviour.value = it
        },
        onToggleSheet = onToggleSheet,
        setLocaleFromString = setLocaleFromString
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenScaffold(
    homeScreenState: HomeScreenState,
    appTheme: MutableState<ThemeState>,
    snackbarHostState: SnackbarHostState,
    appLockBehaviour: MutableState<AppLockBehaviour>,
    sheetState: SheetState,
    currentLocaleList: LocaleListCompat,
    showBottomSheet: Boolean,
    onInfoClick: () -> Unit,
    onSelectProfile: () -> Unit,
    onSelectFavourite: (Configuration) -> Unit,
    onSelectTheme: (ThemeState) -> Unit,
    checkBatteryOptimizationStatus: () -> Boolean,
    onLaunchBatteryOptimizerDisable: () -> Unit,
    onSelectAppLockBehaviour: (AppLockBehaviour) -> Unit,
    onToggleSheet: (Boolean) -> Unit,
    setLocaleFromString: (String) -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(onInfoClick)
        },
        snackbarHost = {
            AppSnackbarHost(snackbarHostState)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.app_settings)) },
                icon = { Icon(Icons.Filled.Settings, null) },
                containerColor = colorScheme.errorContainer,
                contentColor = colorScheme.onErrorContainer,
                onClick = {
                    onToggleSheet(true)
                }
            )
        },
        content = { appPadding ->
            HomeScreenContent(
                appPadding = appPadding,
                homeScreenState = homeScreenState,
                sheetState = sheetState,
                showBottomSheet = showBottomSheet,
                appTheme = appTheme,
                appLockBehaviour = appLockBehaviour,
                currentLocaleList = currentLocaleList,
                onSelectProfile = onSelectProfile,
                onSelectFavourite = onSelectFavourite,
                onSelectTheme = onSelectTheme,
                checkBatteryOptimizationStatus = checkBatteryOptimizationStatus,
                onLaunchBatteryOptimizerDisable = onLaunchBatteryOptimizerDisable,
                onSelectAppLockBehaviour = onSelectAppLockBehaviour,
                onToggleSheet = onToggleSheet,
                setLocaleFromString = setLocaleFromString
            )
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    appPadding: PaddingValues,
    homeScreenState: HomeScreenState,
    sheetState: SheetState,
    showBottomSheet: Boolean,
    appTheme: MutableState<ThemeState>,
    appLockBehaviour: MutableState<AppLockBehaviour>,
    currentLocaleList: LocaleListCompat,
    onSelectProfile: () -> Unit,
    onSelectFavourite: (Configuration) -> Unit,
    onSelectTheme: (ThemeState) -> Unit,
    checkBatteryOptimizationStatus: () -> Boolean,
    onLaunchBatteryOptimizerDisable: () -> Unit,
    onSelectAppLockBehaviour: (AppLockBehaviour) -> Unit,
    onToggleSheet: (Boolean) -> Unit,
    setLocaleFromString: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(appPadding)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            SelectProfileButton(onSelectProfile)
        }

        AppLockBehaviourIndicator(behaviour = appLockBehaviour, toggleBottomSheet = onToggleSheet)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(), verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.quick_start),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = typography.titleLarge
            )
            FavouriteList(
                homeScreenState, onSelectFavourite
            )
        }
    }

    BottomSheet(
        showSheet = showBottomSheet,
        sheetState = sheetState,
        appTheme = appTheme,
        appLockBehaviour = appLockBehaviour,
        onSelectTheme = onSelectTheme,
        onSelectAppLockBehaviour = onSelectAppLockBehaviour,
        checkBatteryOptimizationStatus = checkBatteryOptimizationStatus,
        onLaunchBatteryOptimizerDisable = {
            onToggleSheet(false)
            onLaunchBatteryOptimizerDisable()
        },
        onToggleSheet = onToggleSheet,
        currentLocaleList = currentLocaleList,
        setLocaleFromString = setLocaleFromString
    )
}

@Composable
fun AppLockBehaviourIndicator(
    behaviour: MutableState<AppLockBehaviour>,
    toggleBottomSheet: (Boolean) -> Unit
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
        return // not supported prior to Oreo, so no UI drawn
    }
    OutlinedButton(
        onClick = { toggleBottomSheet(true) },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {
            val imageVector = when (behaviour.value) {
                AppLockBehaviour.NORMAL_BEHAVIOUR -> Icons.Default.Lock
                AppLockBehaviour.ALWAYS_SHOW_WHEN_LOCKED -> Icons.Default.LockOpen
                AppLockBehaviour.ONLY_SHOW_LOCKED_WHEN_RUNNING -> Icons.Default.LockClock
            }
            Icon(imageVector, null, tint = colorScheme.onSurface)
            Text(buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.app_lock_behaviour))
                    append(":")
                }
                append(" ")
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(stringResource(behaviour.value.labelStringRes))
                }
            })
        }
    }
}

@Composable
private fun FavouriteList(
    state: HomeScreenState,
    onSelectFavourite: (Configuration) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val lazyListState = rememberLazyListState()
        val scrollbarsState = rememberScrollbarsState(
            config = ScrollbarsConfig(orientation = ScrollbarsOrientation.Vertical),
            scrollType = ScrollbarsScrollType.Lazy.List.Static(state = lazyListState)
        )
        LazyColumn(
            Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxWidth(),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.favoriteConfigurations.sortedBy { it.name }) { configuration ->
                FavouriteConfigurationCard(
                    configuration = configuration,
                    onSelectFavourite = onSelectFavourite
                )
            }
        }
        Scrollbars(state = scrollbarsState)
    }
}

@Composable
fun FavouriteConfigurationCard(
    configuration: Configuration,
    onSelectFavourite: (Configuration) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorScheme.surfaceContainerHigh,
            contentColor = colorScheme.onSurface
        ),
        shape = shapes.small,
        onClick = {
            onSelectFavourite(configuration)
        }) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Text(
                text = configuration.name, style = typography.titleLarge
            )
            if (configuration.description.isNotBlank()) {
                Text(
                    text = configuration.description,
                    style = typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
private fun SelectProfileButton(onButtonClick: () -> Unit) {
    Button(
        onClick = onButtonClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .testTag(TestTags.HOME_SELECT_CONFIG_BUTTON)
    ) {
        Text(
            text = stringResource(id = R.string.homescreen_btn_select_profile),
            style = typography.headlineMedium
        )
    }
}

@Composable
private fun AppTopBar(onInfoClick: () -> Unit) {
    LocationSimulatorTopBar(
        onBackClick = null,
        title = buildAnnotatedString {
            val appName = stringResource(R.string.app_name)
            val appVersion = stringResource(R.string.app_version, BuildConfig.VERSION_NAME)
            withStyle(ParagraphStyle(textAlign = TextAlign.Center, lineHeight = 20.sp)) {
                withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                    appendLine(appName)
                }
                withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontSize = 14.sp)) {
                    append(appVersion)
                }
            }
        },
        backPossible = false
    ) {
        IconButton(onClick = onInfoClick, modifier = Modifier.padding(5.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_info_24),
                contentDescription = stringResource(
                    id = R.string.about
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@AppPreview
fun HomeScreenPreview() {
    val state by remember {
        mutableStateOf(
            HomeScreenState(
                favoriteConfigurations = PreviewData.previewConfigurations.filter { it.isFavorite },
                configurationsWithErrors = emptyList()
            )
        )
    }
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val themeState = remember {
        mutableStateOf(PreviewData.themePreviewState)
    }
    val appLockBehaviour = remember {
        mutableStateOf(AppLockBehaviour.NORMAL_BEHAVIOUR)
    }
    val sheetState = rememberModalBottomSheetState()
    LocationSimulatorTheme {
        HomeScreenScaffold(
            homeScreenState = state,
            appTheme = themeState,
            snackbarHostState = snackbarHostState,
            onInfoClick = {},
            onSelectProfile = {},
            onSelectFavourite = {},
            onSelectTheme = {},
            checkBatteryOptimizationStatus = { false },
            onLaunchBatteryOptimizerDisable = {},
            appLockBehaviour = appLockBehaviour,
            onSelectAppLockBehaviour = {},
            onToggleSheet = {},
            showBottomSheet = false,
            sheetState = sheetState,
            currentLocaleList = LocaleListCompat.getDefault(),
            setLocaleFromString = {}
        )
    }
}
