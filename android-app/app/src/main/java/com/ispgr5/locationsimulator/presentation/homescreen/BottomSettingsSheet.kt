package com.ispgr5.locationsimulator.presentation.homescreen

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.ispgr5.locationsimulator.R
import com.ispgr5.locationsimulator.presentation.universalComponents.MultiStateToggle
import com.ispgr5.locationsimulator.presentation.util.AppLockBehaviour
import com.ispgr5.locationsimulator.ui.theme.ThemeState
import com.ispgr5.locationsimulator.ui.theme.ThemeType
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    showSheet: Boolean,
    sheetState: SheetState,
    appTheme: MutableState<ThemeState>,
    appLockBehaviour: MutableState<AppLockBehaviour>,
    currentLocaleList: LocaleListCompat,
    onSelectTheme: (ThemeState) -> Unit,
    onSelectAppLockBehaviour: (AppLockBehaviour) -> Unit,
    checkBatteryOptimizationStatus: () -> Boolean,
    onLaunchBatteryOptimizerDisable: () -> Unit,
    onToggleSheet: (Boolean) -> Unit,
    setLocaleFromString: (String) -> Unit
) {
    val nestedScroll = rememberNestedScrollInteropConnection()
    if (!showSheet) return
    ModalBottomSheet(
        onDismissRequest = {
            onToggleSheet(false)
        },
        sheetState = sheetState,
        contentWindowInsets = {
            WindowInsets(top = 50.dp, bottom = 20.dp)
        },
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                width = 100.dp,
                color = colorScheme.primary
            )
        },
        modifier = Modifier.nestedScroll(connection = nestedScroll, dispatcher = null),
    ) {
        Row(modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.app_settings),
                        style = typography.titleLarge.copy(
                            fontSize = typography.titleLarge.fontSize.times(
                                1.3
                            )
                        )
                    )
                }
                item {
                    AppLockBehaviourSelector(appLockBehaviour, onSelectAppLockBehaviour)
                }
                item {
                    ThemeToggle(selectedTheme = appTheme.value, onSetTheme = onSelectTheme)
                }
                item {
                    BatteryOptimizationHint(
                        checkBatteryOptimizationStatus = checkBatteryOptimizationStatus,
                        onLaunchBatteryOptimizerDisable = onLaunchBatteryOptimizerDisable
                    )
                }
                item {
                    LanguageToggle(currentLocaleList) {
                        onToggleSheet(false)
                        setLocaleFromString(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    @StringRes titleStringRes: Int,
    @StringRes subtitleStringRes: Int? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = titleStringRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                textAlign = TextAlign.Center,
                style = typography.titleLarge
            )
            if (subtitleStringRes != null) {
                Text(
                    text = stringResource(id = subtitleStringRes),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    textAlign = TextAlign.Center,
                    style = typography.bodyLarge.copy(fontStyle = FontStyle.Italic)
                )
            }
            content()
        }
    }
}

@Composable
fun ThemeToggle(
    selectedTheme: ThemeState, onSetTheme: (ThemeState) -> Unit
) {
    SettingsCard(
        titleStringRes = R.string.homescreen_app_theme
    ) {
        MultiStateToggle(
            stateKeyLabelMap = ThemeType.entries.associateWith { theme -> theme.labelStringRes },
            selectedOption = selectedTheme.themeType,
            onSelectionChange = { newTheme ->
                onSetTheme(selectedTheme.copy(themeType = newTheme))
            })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColorSchemeToggle(
                useDynamicColors = selectedTheme.useDynamicColor,
                onSelectionChange = { useDynamicColor ->
                    onSetTheme(selectedTheme.copy(useDynamicColor = useDynamicColor))
                }
            )
        }
    }
}

@Composable
fun LanguageToggle(
    currentLocaleList: LocaleListCompat,
    setLocaleFromString: (String) -> Unit
) {
    SettingsCard(
        titleStringRes = R.string.app_language
    ) {
        MultiStateToggle(
            stateKeyLabelMap = mapOf(
                "en-US" to R.string.english,
                "de-DE" to R.string.german
            ),
            selectedOption = currentLocaleList.toLanguageTags()
        ) {
            setLocaleFromString(it)
        }
    }
}


@Composable
fun DynamicColorSchemeToggle(
    useDynamicColors: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
    ) {
        MultiStateToggle(
            stateKeyLabelMap = mapOf(
                false to R.string.default_theme,
                true to R.string.dynamic_theme
            ),
            selectedOption = useDynamicColors,
            onSelectionChange = onSelectionChange
        )
    }
}

@Composable
fun AppLockBehaviourSelector(
    state: MutableState<AppLockBehaviour>,
    onSelect: (AppLockBehaviour) -> Unit
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
        return // not supported prior to Oreo, so no UI drawn
    }
    SettingsCard(
        titleStringRes = R.string.app_lock_behaviour
    ) {
        MultiStateToggle(
            stateKeyLabelMap = AppLockBehaviour.entries.associateWith { it.labelStringRes },
            selectedOption = state.value,
        ) { onSelect(it) }
        Text(
            text = stringResource(state.value.explanationStringRes),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic
        )
    }
}


@Composable
fun BatteryOptimizationHint(
    checkBatteryOptimizationStatus: () -> Boolean,
    onLaunchBatteryOptimizerDisable: () -> Unit
) {
    var isIgnoringOptimization by remember {
        mutableStateOf(checkBatteryOptimizationStatus())
    }
    LaunchedEffect(Unit) {
        while (true) {
            isIgnoringOptimization = checkBatteryOptimizationStatus()
            delay(2000L)
        }
    }
    Crossfade(
        targetState = isIgnoringOptimization,
        label = "battery optimization"
    ) { crossfadedIsIgnoringOptimization ->

        SettingsCard(
            titleStringRes = R.string.battery_opt_title,
            subtitleStringRes = R.string.battery_opt_recommendation
        ) {
            when (crossfadedIsIgnoringOptimization) {
                true -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        Icon(Icons.Default.Cake, null, tint = colorScheme.primary)
                        Text(stringResource(R.string.successfully_disabled))
                    }
                }

                else -> {
                    ElevatedButton(
                        onClick = onLaunchBatteryOptimizerDisable,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = colorScheme.errorContainer,
                            contentColor = colorScheme.onErrorContainer
                        )
                    ) {
                        Text(text = stringResource(id = R.string.battery_opt_button))
                    }
                }
            }
        }
    }
}
