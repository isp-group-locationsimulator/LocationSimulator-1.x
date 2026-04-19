package com.ispgr5.locationsimulator.presentation.select.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ispgr5.locationsimulator.R
import com.ispgr5.locationsimulator.core.util.TestTags
import com.ispgr5.locationsimulator.domain.model.Configuration
import com.ispgr5.locationsimulator.presentation.previewData.PreviewData.previewConfigurations
import com.ispgr5.locationsimulator.ui.theme.LocationSimulatorTheme

/**
 * Shows one Configuration as Button in max width
 */
@Composable
fun RowScope.OneConfigurationListMember(
    configuration: Configuration,
    isToggled: Boolean,
    onToggleClicked: () -> Unit,
    onEditClicked: () -> Unit,
    onSelectClicked: () -> Unit,
    onExportClicked: () -> Unit,
    onDuplicateClicked: () -> Unit,
    hasErrors: Boolean,
    onErrorInfoClicked: () -> Unit,
    isFavorite: Boolean,
    onFavoriteClicked: () -> Unit
) {
    ElevatedCard(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorScheme.surfaceContainerHigh,
            contentColor = colorScheme.onSurface
        ),
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 55.dp)
            .testTag(TestTags.SELECT_CONFIG_BUTTON_PREFIX + configuration.name)
            .clickable(true, onClick = onToggleClicked)

    ) {
        //Column is needed for toggling so the Toggled Information is shown under the Configuration name
        Column(modifier = Modifier.padding(4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                //new row so the Configuration name is centered
                Column(
                    Modifier
                        .weight(8f)
                        .padding(start = 5.dp, end = 5.dp, top = 0.dp, bottom = 0.dp)

                ) {
                    Column {
                        Text(
                            text = configuration.name,
                            fontSize = 18.sp,
                        )
                        if (isToggled) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = configuration.description,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }

                Column(Modifier.weight(1f)) {
                    //showing errors
                    if (hasErrors) {
                        Button(
                            onClick = onErrorInfoClicked,
                            contentPadding = PaddingValues(0.dp),
                            enabled = true,
                            shape = MaterialTheme.shapes.small,
                            border = null,
                            elevation = null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = colorScheme.primary
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.error_24px),
                                contentDescription = null,
                                tint = Color.Red,
                            )
                        }
                    }
                }
                Column(Modifier.weight(1f)) {
                    IconButton(
                        onClick = onFavoriteClicked,
                        enabled = true,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        //favorite
                        if (isFavorite) {
                            Icon(
                                painter = painterResource(id = R.drawable.heart_smile_24px),
                                contentDescription = null,
                                tint = colorScheme.error.copy(alpha = 0.8f)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.favorite_24px),
                                contentDescription = null,
                            )
                        }
                    }
                }

                Column(Modifier.weight(1f)) {
                    IconButton(
                        onClick = onToggleClicked,
                        enabled = true,
                    ) {
                        Icon(
                            painter = if (isToggled) {
                                painterResource(id = R.drawable.keyboard_arrow_down_24px)
                            } else {
                                painterResource(id = R.drawable.keyboard_arrow_right_24px)
                            },
                            contentDescription = null
                        )
                    }
                }
            }
            //The Information which is shown when toggle is active
            if (isToggled) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    //The Select Button
                    IconButton(
                        onClick = onSelectClicked,
                        enabled = !hasErrors,
                        modifier = Modifier.testTag(TestTags.SELECT_CONFIG_BUTTON_SELECT_PREFIX + configuration.name),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.play_arrow_24px),
                            contentDescription = null,
                            tint = colorScheme.onSurface
                        )
                    }
                    //The Export Button
                    IconButton(
                        onClick = onExportClicked,
                        enabled = !hasErrors,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.mobile_share_24px),
                            contentDescription = null,
                            tint = colorScheme.onSurface
                        )
                    }
                    //The Edit Button
                    IconButton(
                        onClick = onEditClicked,
                        modifier = Modifier.testTag(TestTags.SELECT_CONFIG_BUTTON_EDIT_PREFIX + configuration.name),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.edit_24px),
                            contentDescription = null,
                            tint = colorScheme.onSurface
                        )
                    }
                    //The Duplicate Button
                    IconButton(
                        onClick = onDuplicateClicked,
                        enabled = !hasErrors,
                        modifier = Modifier.testTag(TestTags.SELECT_CONFIG_BUTTON_DUPLICTAE_PREFIX + configuration.name),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.content_copy_24px),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ConfigurationListMemberCollapsedPreview() {
    LocationSimulatorTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            OneConfigurationListMember(
                configuration = previewConfigurations.first(),
                isToggled = false,
                onToggleClicked = { },
                onEditClicked = { },
                onSelectClicked = { },
                onExportClicked = { },
                onDuplicateClicked = { },
                hasErrors = false,
                onErrorInfoClicked = { },
                isFavorite = true,
                onFavoriteClicked = {}
            )
        }
    }
}


@Preview
@Composable
fun ConfigurationListMemberExpandedPreview() {
    LocationSimulatorTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            OneConfigurationListMember(
                configuration = previewConfigurations.first(),
                isToggled = true,
                onToggleClicked = { },
                onEditClicked = { },
                onSelectClicked = { },
                onExportClicked = { },
                onDuplicateClicked = { },
                hasErrors = false,
                onErrorInfoClicked = { },
                isFavorite = true,
                onFavoriteClicked = {}
            )
        }
    }
}