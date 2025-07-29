package com.ispgr5.locationsimulator.presentation.universalComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ispgr5.locationsimulator.R
import com.ispgr5.locationsimulator.presentation.previewData.AppPreview


@Composable
@AppPreview
fun MultiStateTogglePreview() {
    var value by remember {
        mutableStateOf("A")
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        MultiStateToggle(
            // random string resources of no significance
            stateKeyLabelMap = mapOf(
                "A" to R.string.app_settings,
                "B" to R.string.app_lock_behaviour,
                "C" to R.string.battery_opt_title
            ),
            selectedOption = value,
            onSelectionChange = {
                value = it
            }
        )
    }
}

@Composable
fun <K> MultiStateToggle(
    modifier: Modifier = Modifier,
    stateKeyLabelMap: Map<K, Int>,
    selectedOption: K,
    onSelectionChange: (K) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        modifier = modifier.wrapContentSize(),
        color = colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(24.dp))
                .height(IntrinsicSize.Min)
                .background(colorScheme.surfaceContainer)
        ) {
            stateKeyLabelMap.entries.forEach { (key, labelStringRes) ->
                Column(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(24.dp))
                        .fillMaxHeight()
                        .clickable {
                            onSelectionChange(key)
                        }
                        .background(
                            when (key) {
                                selectedOption -> {
                                    colorScheme.primary
                                }

                                else -> {
                                    colorScheme.surfaceContainer
                                }
                            }
                        )
                        .padding(
                            vertical = 8.dp,
                            horizontal = 16.dp,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(

                        text = stringResource(id = labelStringRes),
                        textAlign = TextAlign.Center,
                        color = when (key == selectedOption) {
                            true -> colorScheme.onPrimary
                            else -> colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}