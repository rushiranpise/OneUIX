package io.github.soclear.oneuix.ui.category

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.github.soclear.oneuix.R
import io.github.soclear.oneuix.common.NetworkSpeedLayout
import io.github.soclear.oneuix.common.NetworkSpeedMarker
import io.github.soclear.oneuix.common.NetworkSpeedUnit
import io.github.soclear.oneuix.common.ONE_UI_VERSION
import io.github.soclear.oneuix.common.PowerMenuAction
import io.github.soclear.oneuix.common.Preference
import io.github.soclear.oneuix.ui.SettingViewModel
import io.github.soclear.oneuix.ui.component.SelectItem
import io.github.soclear.oneuix.ui.component.SwitchItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import io.github.soclear.oneuix.common.R as CommonR

private const val ESIM_ADAPTER_SIM_BOTH = 2

private val NETWORK_SPEED_TEXT_SIZE_RANGE = 0f..24f
private val NETWORK_SPEED_MARKER_SIZE_RANGE = 0f..32f
private val NETWORK_SPEED_MARKER_GAP_RANGE = 0f..3f
private val NETWORK_SPEED_LINE_SPACING_RANGE = 0f..16f

@Composable
fun DetailPaneSystemUI(
    uiState: Preference.SystemUI,
    onEvent: (SystemUIEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                runCatching { Runtime.getRuntime().exec("su -c killall com.android.systemui") }
            }
        ) {
            Text(text = stringResource(CommonR.string.restartSystemUI))
        }
        DividerText(R.string.status_bar)
        Column {
            var padding by remember {
                mutableFloatStateOf(uiState.statusBar.statusBarLeftPaddingDp)
            }
            var expanded by rememberSaveable { mutableStateOf(false) }

            SwitchItem(
                title = stringResource(id = R.string.statusBarLeftPaddingDp_title),
                modifier = Modifier.animateContentSize(),
                summary = if (uiState.statusBar.modifyStatusBarLeftPadding) {
                    "%.1fdp".format(padding)
                } else null,
                icon = ImageVector.vectorResource(id = R.drawable.padding),
                clickable = true,
                onClick = { expanded = !expanded },
                checked = uiState.statusBar.modifyStatusBarLeftPadding,
                onCheckedChange = {
                    if (it && padding == 0f) {
                        expanded = true
                    } else if (!it) {
                        expanded = false
                    }
                    onEvent(SystemUIEvent.StatusBar.ModifyStatusBarLeftPadding(it))
                }
            )
            AnimatedVisibility(expanded && uiState.statusBar.modifyStatusBarLeftPadding) {
                Slider(
                    value = padding,
                    onValueChange = { padding = it },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    valueRange = 0f..100f,
                    onValueChangeFinished = {
                        onEvent(SystemUIEvent.StatusBar.StatusBarLeftPaddingDp(padding))
                    }
                )
            }
        }
        Column {
            var padding by remember {
                mutableFloatStateOf(uiState.statusBar.statusBarRightPaddingDp)
            }
            var expanded by rememberSaveable { mutableStateOf(false) }

            SwitchItem(
                title = stringResource(id = R.string.statusBarRightPaddingDp_title),
                modifier = Modifier.animateContentSize(),
                summary = if (uiState.statusBar.modifyStatusBarRightPadding) {
                    "%.1fdp".format(padding)
                } else null,
                icon = ImageVector.vectorResource(id = R.drawable.padding),
                clickable = true,
                onClick = { expanded = !expanded },
                checked = uiState.statusBar.modifyStatusBarRightPadding,
                onCheckedChange = {
                    if (it && padding == 0f) {
                        expanded = true
                    } else if (!it) {
                        expanded = false
                    }
                    onEvent(SystemUIEvent.StatusBar.ModifyStatusBarRightPadding(it))
                }
            )
            AnimatedVisibility(expanded && uiState.statusBar.modifyStatusBarRightPadding) {
                Slider(
                    value = padding,
                    onValueChange = { padding = it },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    valueRange = 0f..100f,
                    onValueChangeFinished = {
                        onEvent(SystemUIEvent.StatusBar.StatusBarRightPaddingDp(padding))
                    }
                )
            }
        }
        Column {
            var widthScale by remember {
                mutableFloatStateOf(uiState.statusBar.batteryIconWidthScale)
            }
            var expanded by rememberSaveable { mutableStateOf(false) }

            SwitchItem(
                title = stringResource(id = R.string.setBatteryIconWidthScale_title),
                modifier = Modifier.animateContentSize(),
                summary = if (uiState.statusBar.setBatteryIconWidthScale) {
                    "%.2f".format(widthScale)
                } else null,
                icon = ImageVector.vectorResource(id = R.drawable.battery),
                clickable = true,
                onClick = { expanded = !expanded },
                checked = uiState.statusBar.setBatteryIconWidthScale,
                onCheckedChange = {
                    if (it && widthScale == 0f) {
                        expanded = true
                    } else if (!it) {
                        expanded = false
                    }
                    onEvent(SystemUIEvent.StatusBar.SetBatteryIconWidthScale(it))
                }
            )
            AnimatedVisibility(expanded && uiState.statusBar.setBatteryIconWidthScale) {
                Slider(
                    value = widthScale,
                    onValueChange = { widthScale = it },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    valueRange = 0.5f..2f,
                    onValueChangeFinished = {
                        onEvent(SystemUIEvent.StatusBar.BatteryIconWidthScale(widthScale))
                    }
                )
            }
        }
        Column {
            var heightScale by remember {
                mutableFloatStateOf(uiState.statusBar.batteryIconHeightScale)
            }
            var expanded by rememberSaveable { mutableStateOf(false) }

            SwitchItem(
                title = stringResource(id = R.string.setBatteryIconHeightScale_title),
                modifier = Modifier.animateContentSize(),
                summary = if (uiState.statusBar.setBatteryIconHeightScale) {
                    "%.2f".format(heightScale)
                } else null,
                icon = ImageVector.vectorResource(id = R.drawable.battery),
                clickable = true,
                onClick = { expanded = !expanded },
                checked = uiState.statusBar.setBatteryIconHeightScale,
                onCheckedChange = {
                    if (it && heightScale == 0f) {
                        expanded = true
                    } else if (!it) {
                        expanded = false
                    }
                    onEvent(SystemUIEvent.StatusBar.SetBatteryIconHeightScale(it))
                }
            )
            AnimatedVisibility(expanded && uiState.statusBar.setBatteryIconHeightScale) {
                Slider(
                    value = heightScale,
                    onValueChange = { heightScale = it },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    valueRange = 0.5f..2f,
                    onValueChangeFinished = {
                        onEvent(SystemUIEvent.StatusBar.BatteryIconHeightScale(heightScale))
                    }
                )
            }
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            SwitchItem(
                icon = ImageVector.vectorResource(id = R.drawable.battery),
                title = stringResource(id = R.string.hideBatteryPercentageSign_title),
                summary = stringResource(id = R.string.hideBatteryPercentageSign_summary),
                checked = uiState.statusBar.hideBatteryPercentageSign,
                onCheckedChange = {
                    onEvent(SystemUIEvent.StatusBar.HideBatteryPercentageSign(it))
                }
            )
        }
        SwitchItem(
            icon = ImageVector.vectorResource(id = R.drawable.battery),
            title = stringResource(id = R.string.hideBatteryIcon_title),
            summary = stringResource(id = R.string.hideBatteryIcon_summary),
            checked = uiState.statusBar.hideBatteryIcon,
            onCheckedChange = {
                onEvent(SystemUIEvent.StatusBar.HideBatteryIcon(it))
            }
        )
        if (ONE_UI_VERSION >= 70000) {
            SwitchItem(
                icon = ImageVector.vectorResource(id = R.drawable.battery),
                title = stringResource(id = R.string.addBatteryLevelText_title),
                summary = stringResource(id = R.string.addBatteryLevelText_summary),
                checked = uiState.statusBar.addBatteryLevelText,
                onCheckedChange = {
                    onEvent(SystemUIEvent.StatusBar.AddBatteryLevelText(it))
                }
            )
            AnimatedVisibility(uiState.statusBar.addBatteryLevelText) {
                Column {
                    SwitchItem(
                        icon = ImageVector.vectorResource(id = R.drawable.battery),
                        title = stringResource(id = R.string.hideBatteryLevelTextPercentageSign_title),
                        checked = uiState.statusBar.hideBatteryLevelTextPercentageSign,
                        onCheckedChange = {
                            onEvent(SystemUIEvent.StatusBar.HideBatteryLevelTextPercentageSign(it))
                        }
                    )
                    SwitchItem(
                        icon = ImageVector.vectorResource(id = R.drawable.battery),
                        title = stringResource(id = R.string.hideBatteryLevelTextChargingIcon_title),
                        checked = uiState.statusBar.hideBatteryLevelTextChargingIcon,
                        onCheckedChange = {
                            onEvent(SystemUIEvent.StatusBar.HideBatteryLevelTextChargingIcon(it))
                        }
                    )
                }
            }
        }
        SwitchItem(
            icon = ImageVector.vectorResource(id = R.drawable.net_speed),
            title = stringResource(id = R.string.supportRealTimeNetworkSpeed_title),
            summary = stringResource(id = R.string.supportRealTimeNetworkSpeed_summary),
            checked = uiState.statusBar.supportRealTimeNetworkSpeed,
            onCheckedChange = {
                onEvent(SystemUIEvent.StatusBar.SupportRealTimeNetworkSpeed(it))
            }
        )
        SelectItem(
            icon = ImageVector.vectorResource(id = R.drawable.net_speed),
            title = stringResource(id = R.string.networkSpeedLayout_title),
            entries = listOf(
                stringResource(id = R.string.networkSpeedLayout_systemDefault),
                stringResource(id = R.string.networkSpeedLayout_splitHorizontal),
                stringResource(id = R.string.networkSpeedLayout_splitVertical),
                stringResource(id = R.string.networkSpeedLayout_activeDirection)
            ),
            selectedIndex = NetworkSpeedLayout.coerce(uiState.statusBar.networkSpeedLayout),
            onSelectedIndexChange = {
                onEvent(SystemUIEvent.StatusBar.SetNetworkSpeedLayout(it))
            }
        )
        SwitchItem(
            icon = ImageVector.vectorResource(id = R.drawable.net_speed),
            title = stringResource(id = R.string.showNetworkSpeedArrows_title),
            summary = stringResource(id = R.string.showNetworkSpeedArrows_summary),
            checked = uiState.statusBar.showNetworkSpeedArrows,
            onCheckedChange = {
                onEvent(SystemUIEvent.StatusBar.ShowNetworkSpeedArrows(it))
            }
        )
        SelectItem(
            icon = ImageVector.vectorResource(id = R.drawable.net_speed),
            title = stringResource(id = R.string.networkSpeedMarker_title),
            entries = listOf(
                stringResource(id = R.string.networkSpeedMarker_thinArrows),
                stringResource(id = R.string.networkSpeedMarker_solidTriangles),
                stringResource(id = R.string.networkSpeedMarker_outlineTriangles),
                stringResource(id = R.string.networkSpeedMarker_blockArrows),
                stringResource(id = R.string.networkSpeedMarker_dashedArrows),
                stringResource(id = R.string.networkSpeedMarker_arrowheads),
                stringResource(id = R.string.networkSpeedMarker_single),
                stringResource(id = R.string.networkSpeedMarker_ascii),
                stringResource(id = R.string.networkSpeedMarker_emoji)
            ),
            selectedIndex = NetworkSpeedMarker.coerce(uiState.statusBar.networkSpeedMarker),
            onSelectedIndexChange = {
                onEvent(SystemUIEvent.StatusBar.SetNetworkSpeedMarker(it))
            }
        )
        // Applies everywhere, including the system default, whose own numbers are rewritten when
        // the chosen unit is not the one SystemUI writes.
        SelectItem(
            icon = ImageVector.vectorResource(id = R.drawable.net_speed),
            title = stringResource(id = R.string.networkSpeedUnit_title),
            entries = listOf(
                stringResource(id = R.string.networkSpeedUnit_bytesBinary),
                stringResource(id = R.string.networkSpeedUnit_bytesDecimal),
                stringResource(id = R.string.networkSpeedUnit_bitsDecimal),
                stringResource(id = R.string.networkSpeedUnit_bitsBinary)
            ),
            selectedIndex = NetworkSpeedUnit.coerce(uiState.statusBar.networkSpeedUnit),
            onSelectedIndexChange = {
                onEvent(SystemUIEvent.StatusBar.SetNetworkSpeedUnit(it))
            }
        )
        val systemSizeLabel = stringResource(id = R.string.networkSpeedSize_system)
        val sameAsTextLabel = stringResource(id = R.string.networkSpeedSize_sameAsText)
        // The system default keeps SystemUI's own font and line height, so these two only apply
        // to the split layouts.
        AnimatedVisibility(
            uiState.statusBar.networkSpeedLayout != NetworkSpeedLayout.SYSTEM_DEFAULT
        ) {
            SliderSetting(
                title = stringResource(id = R.string.networkSpeedTextSize_title),
                value = uiState.statusBar.networkSpeedTextSizeSp,
                valueRange = NETWORK_SPEED_TEXT_SIZE_RANGE,
                steps = 23,
                display = { if (it <= 0f) systemSizeLabel else "%.0f sp".format(it) },
                onCommit = {
                    onEvent(SystemUIEvent.StatusBar.NetworkSpeedTextSizeSp(it))
                }
            )
        }
        SliderSetting(
            title = stringResource(id = R.string.networkSpeedMarkerSize_title),
            value = uiState.statusBar.networkSpeedMarkerSizeSp,
            valueRange = NETWORK_SPEED_MARKER_SIZE_RANGE,
            steps = 31,
            display = { if (it <= 0f) sameAsTextLabel else "%.0f sp".format(it) },
            onCommit = {
                onEvent(SystemUIEvent.StatusBar.NetworkSpeedMarkerSizeSp(it))
            }
        )
        SliderSetting(
            title = stringResource(id = R.string.networkSpeedMarkerGap_title),
            value = uiState.statusBar.networkSpeedMarkerGap,
            valueRange = NETWORK_SPEED_MARKER_GAP_RANGE,
            steps = 29,
            display = { "%.2fx".format(it) },
            onCommit = {
                onEvent(SystemUIEvent.StatusBar.NetworkSpeedMarkerGap(it))
            }
        )
        AnimatedVisibility(
            uiState.statusBar.networkSpeedLayout != NetworkSpeedLayout.SYSTEM_DEFAULT
        ) {
            SliderSetting(
                title = stringResource(id = R.string.networkSpeedLineSpacing_title),
                value = uiState.statusBar.networkSpeedLineSpacingDp,
                valueRange = NETWORK_SPEED_LINE_SPACING_RANGE,
                steps = 15,
                display = { if (it <= 0f) systemSizeLabel else "%.0f dp".format(it) },
                onCommit = {
                    onEvent(SystemUIEvent.StatusBar.NetworkSpeedLineSpacingDp(it))
                }
            )
        }
        // The threshold is written in the same unit as the readout, so the two agree.
        val speedUnitPrefix = NetworkSpeedUnit.of(uiState.statusBar.networkSpeedUnit).kiloPrefix
        Column {
            var expanded by rememberSaveable { mutableStateOf(false) }
            var threshold by remember {
                mutableIntStateOf(uiState.statusBar.networkSpeedThreshold)
            }
            SwitchItem(
                icon = ImageVector.vectorResource(id = R.drawable.net_speed),
                title = stringResource(id = R.string.networkSpeedThreshold_title),
                summary = if (uiState.statusBar.networkSpeedThreshold > 0) {
                    "${uiState.statusBar.networkSpeedThreshold} $speedUnitPrefix/s"
                } else null,
                modifier = Modifier.animateContentSize(),
                clickable = true,
                onClick = { expanded = !expanded },
                checked = uiState.statusBar.networkSpeedThreshold > 0,
                onCheckedChange = {
                    if (it && threshold == 0) threshold = 1
                    if (!it) threshold = 0
                    onEvent(SystemUIEvent.StatusBar.NetworkSpeedThreshold(threshold))
                }
            )
            AnimatedVisibility(expanded && uiState.statusBar.networkSpeedThreshold > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    OutlinedTextField(
                        value = threshold.toString(),
                        onValueChange = { threshold = it.toIntOrNull()?.coerceAtLeast(1) ?: 1 },
                        modifier = Modifier.weight(1f),
                        label = {
                            Text(stringResource(id = R.string.networkSpeedThreshold_label, speedUnitPrefix))
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onEvent(SystemUIEvent.StatusBar.NetworkSpeedThreshold(threshold))
                    }) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                }
            }
        }
        Column {
            var expanded by rememberSaveable { mutableStateOf(false) }

            SwitchItem(
                title = stringResource(id = R.string.setStatusBarClockFormat_title),
                modifier = Modifier.animateContentSize(),
                summary = if (uiState.statusBar.setStatusBarClockFormat) {
                    uiState.statusBar.statusBarClockFormat
                } else null,
                icon = ImageVector.vectorResource(id = R.drawable.nest_clock_farsight_digital),
                clickable = true,
                onClick = { expanded = !expanded },
                checked = uiState.statusBar.setStatusBarClockFormat,
                onCheckedChange = {
                    if (it && uiState.statusBar.statusBarClockFormat == "HH:mm") {
                        expanded = true
                    } else if (!it) {
                        expanded = false
                    }
                    onEvent(SystemUIEvent.StatusBar.SetStatusBarClockFormat(it))
                }
            )

            AnimatedVisibility(expanded && uiState.statusBar.setStatusBarClockFormat) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    var tempDataTimeFormat by remember {
                        mutableStateOf(uiState.statusBar.statusBarClockFormat)
                    }
                    var label by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = tempDataTimeFormat,
                        onValueChange = { tempDataTimeFormat = it },
                        modifier = Modifier.weight(1f),
                        label = { Text(text = label) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            var right = true
                            label = try {
                                DateTimeFormatter
                                    .ofPattern(tempDataTimeFormat)
                                    .format(LocalDateTime.now())
                            } catch (_: Throwable) {
                                right = false
                                "error"
                            }
                            if (right) {
                                onEvent(
                                    SystemUIEvent.StatusBar.StatusBarClockFormat(
                                        tempDataTimeFormat
                                    )
                                )
                            }
                        }
                    ) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                }
            }
        }
        Column {
            var scale by remember {
                mutableFloatStateOf(uiState.statusBar.statusBarClockTextScale)
            }
            var expanded by rememberSaveable { mutableStateOf(false) }

            SwitchItem(
                title = stringResource(id = R.string.setStatusBarClockTextScale_title),
                modifier = Modifier.animateContentSize(),
                summary = if (uiState.statusBar.setStatusBarClockTextScale) {
                    "%.2fx".format(scale)
                } else null,
                icon = ImageVector.vectorResource(id = R.drawable.format_size),
                clickable = true,
                onClick = { expanded = !expanded },
                checked = uiState.statusBar.setStatusBarClockTextScale,
                onCheckedChange = {
                    if (it && scale == 1f) {
                        expanded = true
                    } else if (!it) {
                        expanded = false
                    }
                    onEvent(SystemUIEvent.StatusBar.SetStatusBarClockTextScale(it))
                }
            )
            AnimatedVisibility(expanded && uiState.statusBar.setStatusBarClockTextScale) {
                Slider(
                    value = scale,
                    onValueChange = { scale = it },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    valueRange = 0.5f..2.5f,
                    onValueChangeFinished = {
                        onEvent(SystemUIEvent.StatusBar.StatusBarClockTextScale(scale))
                    }
                )
            }
        }
        SwitchItem(
            title = stringResource(id = R.string.updateStatusBarClockEverySecond_title),
            summary = stringResource(id = R.string.updateStatusBarClockEverySecond_summary),
            icon = ImageVector.vectorResource(id = R.drawable.nest_clock_farsight_digital),
            checked = uiState.statusBar.updateStatusBarClockEverySecond,
            onCheckedChange = {
                onEvent(SystemUIEvent.StatusBar.UpdateStatusBarClockEverySecond(it))
            }
        )
        SwitchItem(
            icon = ImageVector.vectorResource(id = R.drawable.folder_managed),
            title = stringResource(id = R.string.hideSecureFolderStatusBarIcon_title),
            checked = uiState.statusBar.hideSecureFolderStatusBarIcon,
            onCheckedChange = {
                onEvent(SystemUIEvent.StatusBar.HideSecureFolderStatusBarIcon(it))
            }
        )
        SwitchItem(
            icon = ImageVector.vectorResource(id = R.drawable.bluetooth),
            title = stringResource(id = R.string.restoreBluetoothStatusBarIcon_title),
            checked = uiState.statusBar.restoreBluetoothStatusBarIcon,
            onCheckedChange = {
                onEvent(SystemUIEvent.StatusBar.RestoreBluetoothStatusBarIcon(it))
            }
        )
        SwitchItem(
            icon = ImageVector.vectorResource(id = R.drawable.sim_card),
            title = stringResource(id = R.string.physicalEsimAdapterWorkaround_title),
            summary = stringResource(id = R.string.physicalEsimAdapterWorkaround_summary),
            checked = uiState.statusBar.physicalEsimAdapterWorkaround,
            onCheckedChange = {
                onEvent(SystemUIEvent.StatusBar.PhysicalEsimAdapterWorkaround(it))
            }
        )
        AnimatedVisibility(uiState.statusBar.physicalEsimAdapterWorkaround) {
            SelectItem(
                icon = ImageVector.vectorResource(id = R.drawable.sim_card),
                title = stringResource(id = R.string.physicalEsimAdapterSimSlot_title),
                entries = listOf(
                    stringResource(id = R.string.sim_slot_1),
                    stringResource(id = R.string.sim_slot_2),
                    stringResource(id = R.string.sim_slot_both)
                ),
                selectedIndex = uiState.statusBar.physicalEsimAdapterSimSlot.coerceIn(
                    0,
                    ESIM_ADAPTER_SIM_BOTH
                ),
                onSelectedIndexChange = {
                    onEvent(SystemUIEvent.StatusBar.PhysicalEsimAdapterSimSlot(it))
                }
            )
        }
        SwitchItem(
            icon = ImageVector.vectorResource(id = R.drawable.mobile_screensaver),
            title = stringResource(id = R.string.doubleTapStatusBarToSleep_title),
            checked = uiState.statusBar.doubleTapStatusBarToSleep,
            onCheckedChange = {
                onEvent(SystemUIEvent.StatusBar.DoubleTapStatusBarToSleep(it))
            }
        )
        Column {
            var max by remember {
                mutableIntStateOf(uiState.statusBar.statusBarMaxNotificationIcons)
            }
            var expanded by rememberSaveable { mutableStateOf(false) }

            SwitchItem(
                title = stringResource(id = R.string.setStatusBarMaxNotificationIcons_title),
                modifier = Modifier.animateContentSize(),
                summary = if (uiState.statusBar.modifyStatusBarMaxNotificationIcons) " $max" else null,
                icon = ImageVector.vectorResource(id = R.drawable.notifications),
                clickable = true,
                onClick = { expanded = !expanded },
                checked = uiState.statusBar.modifyStatusBarMaxNotificationIcons,
                onCheckedChange = {
                    if (it && max == 4) {
                        expanded = true
                    } else if (!it) {
                        expanded = false
                    }
                    onEvent(SystemUIEvent.StatusBar.ModifyStatusBarMaxNotificationIcons(it))
                }
            )
            AnimatedVisibility(expanded && uiState.statusBar.modifyStatusBarMaxNotificationIcons) {
                Slider(
                    value = max.toFloat(),
                    onValueChange = { max = it.roundToInt() },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    valueRange = 4f..20f,
                    steps = 15,
                    onValueChangeFinished = {
                        onEvent(SystemUIEvent.StatusBar.StatusBarMaxNotificationIcons(max))
                    }
                )
            }
        }
        Column {
            var expanded by rememberSaveable { mutableStateOf(false) }

            SwitchItem(
                title = stringResource(id = R.string.setCustomCarrierName_title),
                modifier = Modifier.animateContentSize(),
                summary = if (uiState.statusBar.setCustomCarrierName) {
                    uiState.statusBar.customCarrierName
                } else null,
                icon = ImageVector.vectorResource(id = R.drawable.sim_card),
                clickable = true,
                onClick = { expanded = !expanded },
                checked = uiState.statusBar.setCustomCarrierName,
                onCheckedChange = {
                    if (it && uiState.statusBar.customCarrierName.isEmpty()) {
                        expanded = true
                    } else if (!it) {
                        expanded = false
                    }
                    onEvent(SystemUIEvent.StatusBar.SetCustomCarrierName(it))
                }
            )

            AnimatedVisibility(expanded && uiState.statusBar.setCustomCarrierName) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    var tempCustomCarrierName by remember {
                        mutableStateOf(uiState.statusBar.customCarrierName)
                    }

                    OutlinedTextField(
                        value = tempCustomCarrierName,
                        onValueChange = { tempCustomCarrierName = it },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onEvent(
                                SystemUIEvent.StatusBar.CustomCarrierName(
                                    tempCustomCarrierName
                                )
                            )
                        }
                    ) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                }
            }
        }
        SwitchItem(
            icon = ImageVector.vectorResource(id = R.drawable.mobile_screensaver),
            title = stringResource(id = R.string.hideLockscreenStatusBar_title),
            checked = uiState.statusBar.hideLockscreenStatusBar,
            onCheckedChange = {
                onEvent(SystemUIEvent.StatusBar.HideLockscreenStatusBar(it))
            }
        )

        DividerText(R.string.qs)
        SwitchItem(
            icon = ImageVector.vectorResource(id = R.drawable.format_letter_spacing),
            title = stringResource(id = R.string.setQsClockMonospaced_title),
            checked = uiState.qs.setQsClockMonospaced,
            onCheckedChange = {
                onEvent(SystemUIEvent.QS.SetQsClockMonospaced(it))
            }
        )
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            SwitchItem(
                icon = ImageVector.vectorResource(id = R.drawable.tile_medium),
                title = stringResource(id = R.string.hideDeviceControlQsTile_title),
                checked = uiState.qs.hideDeviceControlQsTile,
                onCheckedChange = {
                    onEvent(SystemUIEvent.QS.HideDeviceControlQsTile(it))
                }
            )
            SwitchItem(
                icon = ImageVector.vectorResource(id = R.drawable.tile_medium),
                title = stringResource(id = R.string.hideSmartViewQsTile_title),
                checked = uiState.qs.hideSmartViewQsTile,
                onCheckedChange = {
                    onEvent(SystemUIEvent.QS.HideSmartViewQsTile(it))
                }
            )
            SwitchItem(
                icon = ImageVector.vectorResource(id = R.drawable.five_g),
                title = stringResource(id = R.string.turnOn5gQsTile_title),
                summary = stringResource(id = R.string.turnOn5gQsTile_summary),
                checked = uiState.qs.turnOn5gQsTile,
                onCheckedChange = {
                    onEvent(SystemUIEvent.QS.TurnOn5gQsTile(it))
                }
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.turnOn5gQsTile_title)) },
                leadingContent = {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.five_g),
                        stringResource(id = R.string.turnOn5gQsTile_title)
                    )
                },
                supportingContent = { Text(stringResource(id = R.string.root5gQsTile_summary)) }
            )
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.immersiveModeQsTile_title)) },
                leadingContent = {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.immersive_mode),
                        stringResource(id = R.string.immersiveModeQsTile_title)
                    )
                },
                supportingContent = { Text(stringResource(id = R.string.rootImmersiveModeQsTile_summary)) }
            )
            if (ONE_UI_VERSION < 80500) {
                SwitchItem(
                    icon = ImageVector.vectorResource(id = R.drawable.tile_medium),
                    title = stringResource(id = R.string.hideQsBarMediaPlayer_title),
                    checked = uiState.qs.hideQsBarMediaPlayer,
                    onCheckedChange = {
                        onEvent(SystemUIEvent.QS.HideQsBarMediaPlayer(it))
                    }
                )
            }
            if (ONE_UI_VERSION < 80500) {
                SwitchItem(
                    icon = ImageVector.vectorResource(id = R.drawable.tile_medium),
                    title = stringResource(id = R.string.hideQsBarNearbyDevicesAndDeviceControl_title),
                    checked = uiState.qs.hideQsBarNearbyDevicesAndDeviceControl,
                    onCheckedChange = {
                        onEvent(SystemUIEvent.QS.HideQsBarNearbyDevicesAndDeviceControl(it))
                    }
                )
            }
            SwitchItem(
                icon = ImageVector.vectorResource(id = R.drawable.tile_medium),
                title = stringResource(id = R.string.hideQsBarSecurityFooter_title),
                summary = stringResource(id = R.string.hideQsBarSecurityFooter_summary),
                checked = uiState.qs.hideQsBarSecurityFooter,
                onCheckedChange = {
                    onEvent(SystemUIEvent.QS.HideQsBarSecurityFooter(it))
                }
            )
            SwitchItem(
                icon = ImageVector.vectorResource(id = R.drawable.tile_medium),
                title = stringResource(id = R.string.hideQsBarDataUsage_title),
                checked = uiState.qs.hideQsBarDataUsage,
                onCheckedChange = {
                    onEvent(SystemUIEvent.QS.HideQsBarDataUsage(it))
                }
            )
            if (ONE_UI_VERSION < 80500) {
                SwitchItem(
                    icon = ImageVector.vectorResource(id = R.drawable.tile_medium),
                    title = stringResource(id = R.string.hideQsBarSmartViewAndModes_title),
                    checked = uiState.qs.hideQsBarSmartViewAndModes,
                    onCheckedChange = {
                        onEvent(SystemUIEvent.QS.HideQsBarSmartViewAndModes(it))
                    }
                )
            }
            if (ONE_UI_VERSION < 80500) {
                SwitchItem(
                    icon = ImageVector.vectorResource(id = R.drawable.expand),
                    title = stringResource(id = R.string.alwaysExpandQsTileChunk_title),
                    checked = uiState.qs.alwaysExpandQsTileChunk,
                    onCheckedChange = {
                        onEvent(SystemUIEvent.QS.AlwaysExpandQsTileChunk(it))
                    }
                )
            }
            if (ONE_UI_VERSION < 80500) {
                SwitchItem(
                    title = stringResource(id = R.string.alwaysShowTimeDateOnQs_title),
                    icon = ImageVector.vectorResource(id = R.drawable.nest_clock_farsight_digital),
                    checked = uiState.qs.alwaysShowTimeDateOnQs,
                    onCheckedChange = {
                        onEvent(SystemUIEvent.QS.AlwaysShowTimeDateOnQs(it))
                    }
                )
            }
            if (ONE_UI_VERSION < 80500) {
                SwitchItem(
                    icon = ImageVector.vectorResource(id = R.drawable.light_mode),
                    title = stringResource(id = R.string.addBrightnessProgressToQsBar_title),
                    checked = uiState.qs.addBrightnessProgressToQsBar,
                    onCheckedChange = {
                        onEvent(SystemUIEvent.QS.AddBrightnessProgressToQsBar(it))
                    }
                )
            }
            if (ONE_UI_VERSION < 80500) {
                SwitchItem(
                    icon = ImageVector.vectorResource(id = R.drawable.music_note),
                    title = stringResource(id = R.string.addVolumeProgressToQsBar_title),
                    checked = uiState.qs.addVolumeProgressToQsBar,
                    onCheckedChange = {
                        onEvent(SystemUIEvent.QS.AddVolumeProgressToQsBar(it))
                    }
                )
            }
            SwitchItem(
                icon = ImageVector.vectorResource(id = R.drawable.today),
                title = stringResource(id = R.string.showTraditionalChineseDateOnQS_title),
                checked = uiState.qs.showTraditionalChineseDateOnQS,
                onCheckedChange = {
                    onEvent(SystemUIEvent.QS.ShowTraditionalChineseDateOnQS(it))
                }
            )
            Column {
                var textSize by remember { mutableFloatStateOf(uiState.qs.qsClockTextSize) }
                var expanded by rememberSaveable { mutableStateOf(false) }

                SwitchItem(
                    title = stringResource(id = R.string.modifyQSClockTextSize_title),
                    modifier = Modifier.animateContentSize(),
                    summary = if (uiState.qs.modifyQSClockTextSize) {
                        " %.1fsp".format(textSize)
                    } else null,
                    icon = ImageVector.vectorResource(id = R.drawable.format_size),
                    clickable = true,
                    onClick = { expanded = !expanded },
                    checked = uiState.qs.modifyQSClockTextSize,
                    onCheckedChange = {
                        if (it && textSize == 32f) {
                            expanded = true
                        } else if (!it) {
                            expanded = false
                        }
                        onEvent(SystemUIEvent.QS.ModifyQSClockTextSize(it))
                    }
                )

                AnimatedVisibility(expanded && uiState.qs.modifyQSClockTextSize) {
                    Slider(
                        value = textSize,
                        onValueChange = { textSize = it },
                        modifier = Modifier.padding(horizontal = 16.dp),
                        valueRange = 15f..70f,
                        onValueChangeFinished = {
                            onEvent(SystemUIEvent.QS.QSClockTextSize(textSize))
                        }
                    )
                }
            }


            DividerText(R.string.aod)
            SwitchItem(
                icon = ImageVector.vectorResource(id = R.drawable.battery),
                title = stringResource(id = R.string.hideAODStatusBar_title),
                checked = uiState.aod.hideAODStatusBar,
                onCheckedChange = {
                    onEvent(SystemUIEvent.AOD.HideAODStatusBar(it))
                }
            )
            SwitchItem(
                icon = ImageVector.vectorResource(id = R.drawable.today),
                title = stringResource(id = R.string.aodLockSupportLunar_title),
                checked = uiState.aod.aodLockSupportLunar,
                onCheckedChange = {
                    onEvent(SystemUIEvent.AOD.AODLockSupportLunar(it))
                }
            )
        }

        DividerText(R.string.notification)
        Column {
            var expanded by rememberSaveable { mutableStateOf(false) }
            SwitchItem(
                icon = ImageVector.vectorResource(id = R.drawable.music_note),
                title = stringResource(id = R.string.hideOngoingActivityMedia_title),
                summary = if (uiState.notification.hideOngoingActivityMedia && uiState.notification.hideOngoingActivityMediaPackages.isNotEmpty()) {
                    uiState.notification.hideOngoingActivityMediaPackages
                } else {
                    stringResource(id = R.string.hideOngoingActivityMedia_summary)
                },
                clickable = true,
                onClick = { expanded = !expanded },
                checked = uiState.notification.hideOngoingActivityMedia,
                onCheckedChange = {
                    if (it && uiState.notification.hideOngoingActivityMediaPackages.isEmpty()) {
                        expanded = true
                    } else if (!it) {
                        expanded = false
                    }
                    onEvent(SystemUIEvent.Notification.HideOngoingActivityMedia(it))
                }
            )
            AnimatedVisibility(expanded && uiState.notification.hideOngoingActivityMedia) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    var tempPackages by remember {
                        mutableStateOf(uiState.notification.hideOngoingActivityMediaPackages)
                    }
                    OutlinedTextField(
                        value = tempPackages,
                        onValueChange = { tempPackages = it },
                        modifier = Modifier.weight(1f),
                        label = { Text(text = stringResource(id = R.string.hideOngoingActivityMedia_packages_hint)) },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onEvent(SystemUIEvent.Notification.HideOngoingActivityMediaPackages(tempPackages))
                        }
                    ) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                }
            }
        }
        SwitchItem(
            icon = ImageVector.vectorResource(id = R.drawable.notifications),
            title = stringResource(id = R.string.disableNotificationGrouping_title),
            summary = stringResource(id = R.string.disableNotificationGrouping_summary),
            checked = uiState.notification.disableNotificationGrouping,
            onCheckedChange = {
                onEvent(SystemUIEvent.Notification.DisableNotificationGrouping(it))
            }
        )
        SwitchItem(
            icon = ImageVector.vectorResource(id = R.drawable.notifications),
            title = stringResource(id = R.string.autoExpandNotifications_title),
            checked = uiState.notification.autoExpandNotifications,
            onCheckedChange = {
                onEvent(SystemUIEvent.Notification.AutoExpandNotifications(it))
            }
        )

        DividerText(R.string.other)
        Column {
            var expanded by rememberSaveable { mutableStateOf(false) }
            SwitchItem(
                icon = ImageVector.vectorResource(id = R.drawable.power_settings_new),
                title = stringResource(id = R.string.customPowerMenu_title),
                clickable = true,
                onClick = { expanded = !expanded },
                checked = uiState.other.customPowerMenu,
                onCheckedChange = {
                    expanded = it
                    onEvent(SystemUIEvent.Other.CustomPowerMenu(it))
                }
            )
            AnimatedVisibility(expanded && uiState.other.customPowerMenu) {
                PowerMenuActionEditor(
                    actions = uiState.other.powerMenuActions,
                    onActionsChange = {
                        onEvent(SystemUIEvent.Other.PowerMenuActions(it))
                    }
                )
            }
        }
        SwitchItem(
            icon = ImageVector.vectorResource(id = R.drawable.screenshot),
            title = stringResource(id = R.string.disableScreenshotCaptureSound_title),
            checked = uiState.other.disableScreenshotCaptureSound,
            onCheckedChange = {
                onEvent(SystemUIEvent.Other.DisableScreenshotCaptureSound(it))
            }
        )
    }
}

@Composable
private fun SliderSetting(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    display: (Float) -> String,
    onCommit: (Float) -> Unit,
) {
    var current by remember { mutableFloatStateOf(value) }
    Column {
        ListItem(
            headlineContent = { Text(text = title) },
            supportingContent = { Text(text = display(current)) },
        )
        Slider(
            value = current,
            onValueChange = { current = it },
            onValueChangeFinished = { onCommit(current) },
            modifier = Modifier.padding(horizontal = 16.dp),
            valueRange = valueRange,
            steps = steps,
        )
    }
}

@Composable
private fun DividerText(@StringRes id: Int) = Text(
    text = stringResource(id),
    modifier = Modifier.padding(start = 16.dp, top = 32.dp, end = 16.dp),
    color = MaterialTheme.colorScheme.primary,
)

@Composable
private fun PowerMenuActionEditor(
    actions: List<PowerMenuAction>,
    onActionsChange: (List<PowerMenuAction>) -> Unit,
) {
    val normalizedActions = PowerMenuAction.normalize(actions)
    Column {
        normalizedActions.forEachIndexed { index, action ->
            val visiblePosition = normalizedActions.take(index + 1).count { it.visible }
            ListItem(
                headlineContent = {
                    Text(text = stringResource(id = powerMenuActionTitle(action.name)))
                },
                supportingContent = {
                    Text(
                        text = if (action.visible) {
                            stringResource(
                                id = R.string.powerMenuActionVisible_summary,
                                visiblePosition
                            )
                        } else {
                            stringResource(id = R.string.powerMenuActionHidden_summary)
                        }
                    )
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            enabled = index > 0,
                            onClick = {
                                onActionsChange(normalizedActions.move(index, index - 1))
                            }
                        ) {
                            Text(text = stringResource(id = R.string.moveUp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            enabled = index < normalizedActions.lastIndex,
                            onClick = {
                                onActionsChange(normalizedActions.move(index, index + 1))
                            }
                        ) {
                            Text(text = stringResource(id = R.string.moveDown))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = action.visible,
                            onCheckedChange = { visible ->
                                onActionsChange(
                                    normalizedActions.mapIndexed { actionIndex, item ->
                                        if (actionIndex == index) item.copy(visible = visible) else item
                                    }
                                )
                            }
                        )
                    }
                }
            )
        }
    }
}

private fun List<PowerMenuAction>.move(fromIndex: Int, toIndex: Int): List<PowerMenuAction> =
    toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }

@StringRes
private fun powerMenuActionTitle(actionName: String): Int = when (actionName) {
    PowerMenuAction.POWER -> R.string.powerMenuAction_power
    PowerMenuAction.DATA_MODE -> R.string.powerMenuAction_dataMode
    PowerMenuAction.RESTART -> R.string.powerMenuAction_restart
    PowerMenuAction.SAFE_MODE -> R.string.powerMenuAction_safeMode
    PowerMenuAction.LOCK_DOWN_MODE -> R.string.powerMenuAction_lockDownMode
    PowerMenuAction.EMERGENCY_CALL -> R.string.powerMenuAction_emergencyCall
    PowerMenuAction.MEDICAL_INFO -> R.string.powerMenuAction_medicalInfo
    PowerMenuAction.SIDE_KEY_SETTINGS -> R.string.sideKeySettings
    PowerMenuAction.FORCE_RESTART_MESSAGE -> R.string.powerMenuAction_forceRestartMessage
    PowerMenuAction.RESTART_SYSTEMUI -> CommonR.string.restartSystemUI
    PowerMenuAction.RESTART_RECOVERY -> CommonR.string.restartRecovery
    PowerMenuAction.RESTART_DOWNLOAD -> CommonR.string.restartDownload
    PowerMenuAction.SOFT_REBOOT -> CommonR.string.softReboot
    else -> R.string.other
}

sealed interface SystemUIEvent {
    sealed interface StatusBar : SystemUIEvent {
        @JvmInline
        value class ModifyStatusBarLeftPadding(val value: Boolean) : StatusBar

        @JvmInline
        value class StatusBarLeftPaddingDp(val value: Float) : StatusBar

        @JvmInline
        value class ModifyStatusBarRightPadding(val value: Boolean) : StatusBar

        @JvmInline
        value class StatusBarRightPaddingDp(val value: Float) : StatusBar

        @JvmInline
        value class SetBatteryIconWidthScale(val value: Boolean) : StatusBar

        @JvmInline
        value class BatteryIconWidthScale(val value: Float) : StatusBar

        @JvmInline
        value class SetBatteryIconHeightScale(val value: Boolean) : StatusBar

        @JvmInline
        value class BatteryIconHeightScale(val value: Float) : StatusBar

        @JvmInline
        value class HideBatteryPercentageSign(val value: Boolean) : StatusBar

        @JvmInline
        value class HideBatteryIcon(val value: Boolean) : StatusBar

        @JvmInline
        value class AddBatteryLevelText(val value: Boolean) : StatusBar

        @JvmInline
        value class HideBatteryLevelTextPercentageSign(val value: Boolean) : StatusBar

        @JvmInline
        value class HideBatteryLevelTextChargingIcon(val value: Boolean) : StatusBar

        @JvmInline
        value class SupportRealTimeNetworkSpeed(val value: Boolean) : StatusBar

        @JvmInline
        value class ShowNetworkSpeedArrows(val value: Boolean) : StatusBar

        @JvmInline
        value class SetNetworkSpeedLayout(val value: Int) : StatusBar

        @JvmInline
        value class SetNetworkSpeedMarker(val value: Int) : StatusBar

        @JvmInline
        value class SetNetworkSpeedUnit(val value: Int) : StatusBar

        @JvmInline
        value class NetworkSpeedTextSizeSp(val value: Float) : StatusBar

        @JvmInline
        value class NetworkSpeedMarkerSizeSp(val value: Float) : StatusBar

        @JvmInline
        value class NetworkSpeedMarkerGap(val value: Float) : StatusBar

        @JvmInline
        value class NetworkSpeedLineSpacingDp(val value: Float) : StatusBar

        @JvmInline
        value class NetworkSpeedThreshold(val value: Int) : StatusBar

        @JvmInline
        value class SetStatusBarClockFormat(val value: Boolean) : StatusBar

        @JvmInline
        value class StatusBarClockFormat(val value: String) : StatusBar

        @JvmInline
        value class SetStatusBarClockTextScale(val value: Boolean) : StatusBar

        @JvmInline
        value class StatusBarClockTextScale(val value: Float) : StatusBar

        @JvmInline
        value class UpdateStatusBarClockEverySecond(val value: Boolean) : StatusBar

        @JvmInline
        value class HideSecureFolderStatusBarIcon(val value: Boolean) : StatusBar

        @JvmInline
        value class RestoreBluetoothStatusBarIcon(val value: Boolean) : StatusBar

        @JvmInline
        value class PhysicalEsimAdapterWorkaround(val value: Boolean) : StatusBar

        @JvmInline
        value class PhysicalEsimAdapterSimSlot(val value: Int) : StatusBar

        @JvmInline
        value class DoubleTapStatusBarToSleep(val value: Boolean) : StatusBar

        @JvmInline
        value class ModifyStatusBarMaxNotificationIcons(val value: Boolean) : StatusBar

        @JvmInline
        value class StatusBarMaxNotificationIcons(val value: Int) : StatusBar

        @JvmInline
        value class SetCustomCarrierName(val value: Boolean) : StatusBar

        @JvmInline
        value class CustomCarrierName(val value: String) : StatusBar

        @JvmInline
        value class HideLockscreenStatusBar(val value: Boolean) : StatusBar
    }

    sealed interface QS : SystemUIEvent {
        @JvmInline
        value class SetQsClockMonospaced(val value: Boolean) : QS

        @JvmInline
        value class HideDeviceControlQsTile(val value: Boolean) : QS

        @JvmInline
        value class HideSmartViewQsTile(val value: Boolean) : QS

        @JvmInline
        value class TurnOn5gQsTile(val value: Boolean) : QS

        @JvmInline
        value class HideQsBarMediaPlayer(val value: Boolean) : QS

        @JvmInline
        value class HideQsBarNearbyDevicesAndDeviceControl(val value: Boolean) : QS

        @JvmInline
        value class HideQsBarSecurityFooter(val value: Boolean) : QS

        @JvmInline
        value class HideQsBarDataUsage(val value: Boolean) : QS

        @JvmInline
        value class HideQsBarSmartViewAndModes(val value: Boolean) : QS

        @JvmInline
        value class AlwaysExpandQsTileChunk(val value: Boolean) : QS

        @JvmInline
        value class AlwaysShowTimeDateOnQs(val value: Boolean) : QS

        @JvmInline
        value class AddBrightnessProgressToQsBar(val value: Boolean) : QS

        @JvmInline
        value class AddVolumeProgressToQsBar(val value: Boolean) : QS

        @JvmInline
        value class ShowTraditionalChineseDateOnQS(val value: Boolean) : QS

        @JvmInline
        value class ModifyQSClockTextSize(val value: Boolean) : QS

        @JvmInline
        value class QSClockTextSize(val value: Float) : QS
    }

    sealed interface AOD : SystemUIEvent {
        @JvmInline
        value class HideAODStatusBar(val value: Boolean) : AOD

        @JvmInline
        value class AODLockSupportLunar(val value: Boolean) : AOD
    }

    sealed interface Notification : SystemUIEvent {
        @JvmInline
        value class HideOngoingActivityMedia(val value: Boolean) : Notification

        @JvmInline
        value class HideOngoingActivityMediaPackages(val value: String) : Notification

        @JvmInline
        value class DisableNotificationGrouping(val value: Boolean) : Notification

        @JvmInline
        value class AutoExpandNotifications(val value: Boolean) : Notification
    }

    sealed interface Other : SystemUIEvent {
        @JvmInline
        value class CustomPowerMenu(val value: Boolean) : Other

        @JvmInline
        value class PowerMenuActions(val value: List<PowerMenuAction>) : Other

        @JvmInline
        value class DisableScreenshotCaptureSound(val value: Boolean) : Other
    }
}

fun SettingViewModel.onSystemUIEvent(event: SystemUIEvent) {
    when (event) {
        is SystemUIEvent.StatusBar -> onStatusBarEvent(event)
        is SystemUIEvent.QS -> onQSEvent(event)
        is SystemUIEvent.AOD -> onAODEvent(event)
        is SystemUIEvent.Notification -> onNotification(event)
        is SystemUIEvent.Other -> onOtherEvent(event)
    }
}

private fun SettingViewModel.onStatusBarEvent(event: SystemUIEvent.StatusBar) {
    updateData { preference ->
        when (event) {
            is SystemUIEvent.StatusBar.ModifyStatusBarLeftPadding -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            modifyStatusBarLeftPadding = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.StatusBarLeftPaddingDp -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            statusBarLeftPaddingDp = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.ModifyStatusBarRightPadding -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            modifyStatusBarRightPadding = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.StatusBarRightPaddingDp -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            statusBarRightPaddingDp = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.SetBatteryIconWidthScale -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            setBatteryIconWidthScale = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.BatteryIconWidthScale -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            batteryIconWidthScale = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.HideLockscreenStatusBar -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            hideLockscreenStatusBar = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.SetBatteryIconHeightScale -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            setBatteryIconHeightScale = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.BatteryIconHeightScale -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            batteryIconHeightScale = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.HideBatteryPercentageSign -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            hideBatteryPercentageSign = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.HideBatteryIcon -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            hideBatteryIcon = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.AddBatteryLevelText -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            addBatteryLevelText = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.HideBatteryLevelTextPercentageSign -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            hideBatteryLevelTextPercentageSign = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.HideBatteryLevelTextChargingIcon -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            hideBatteryLevelTextChargingIcon = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.SupportRealTimeNetworkSpeed -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            supportRealTimeNetworkSpeed = event.value
                        )
                    )
                )
            }


            is SystemUIEvent.StatusBar.SetNetworkSpeedLayout -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            networkSpeedLayout = NetworkSpeedLayout.coerce(event.value)
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.SetNetworkSpeedMarker -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            networkSpeedMarker = NetworkSpeedMarker.coerce(event.value)
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.SetNetworkSpeedUnit -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            networkSpeedUnit = NetworkSpeedUnit.coerce(event.value)
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.NetworkSpeedTextSizeSp -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            networkSpeedTextSizeSp = event.value.coerceIn(
                                NETWORK_SPEED_TEXT_SIZE_RANGE
                            )
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.NetworkSpeedMarkerSizeSp -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            networkSpeedMarkerSizeSp = event.value.coerceIn(
                                NETWORK_SPEED_MARKER_SIZE_RANGE
                            )
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.NetworkSpeedMarkerGap -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            networkSpeedMarkerGap = event.value.coerceIn(
                                NETWORK_SPEED_MARKER_GAP_RANGE
                            )
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.NetworkSpeedLineSpacingDp -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            networkSpeedLineSpacingDp = event.value.coerceIn(
                                NETWORK_SPEED_LINE_SPACING_RANGE
                            )
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.ShowNetworkSpeedArrows -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            showNetworkSpeedArrows = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.NetworkSpeedThreshold -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            networkSpeedThreshold = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.SetStatusBarClockFormat -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            setStatusBarClockFormat = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.StatusBarClockFormat -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            statusBarClockFormat = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.SetStatusBarClockTextScale -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            setStatusBarClockTextScale = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.StatusBarClockTextScale -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            statusBarClockTextScale = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.UpdateStatusBarClockEverySecond -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            updateStatusBarClockEverySecond = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.HideSecureFolderStatusBarIcon -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            hideSecureFolderStatusBarIcon = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.RestoreBluetoothStatusBarIcon -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            restoreBluetoothStatusBarIcon = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.PhysicalEsimAdapterWorkaround -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            physicalEsimAdapterWorkaround = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.PhysicalEsimAdapterSimSlot -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            physicalEsimAdapterSimSlot = event.value.coerceIn(
                                0,
                                ESIM_ADAPTER_SIM_BOTH
                            )
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.DoubleTapStatusBarToSleep -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            doubleTapStatusBarToSleep = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.ModifyStatusBarMaxNotificationIcons -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            modifyStatusBarMaxNotificationIcons = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.StatusBarMaxNotificationIcons -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            statusBarMaxNotificationIcons = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.SetCustomCarrierName -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            setCustomCarrierName = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.StatusBar.CustomCarrierName -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        statusBar = preference.systemUI.statusBar.copy(
                            customCarrierName = event.value
                        )
                    )
                )
            }
        }
    }
}

private fun SettingViewModel.onQSEvent(event: SystemUIEvent.QS) {
    updateData { preference ->
        when (event) {
            is SystemUIEvent.QS.SetQsClockMonospaced -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            setQsClockMonospaced = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.HideDeviceControlQsTile -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            hideDeviceControlQsTile = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.HideSmartViewQsTile -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            hideSmartViewQsTile = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.TurnOn5gQsTile -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            turnOn5gQsTile = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.HideQsBarMediaPlayer -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            hideQsBarMediaPlayer = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.HideQsBarNearbyDevicesAndDeviceControl -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            hideQsBarNearbyDevicesAndDeviceControl = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.HideQsBarSecurityFooter -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            hideQsBarSecurityFooter = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.HideQsBarDataUsage -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            hideQsBarDataUsage = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.HideQsBarSmartViewAndModes -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            hideQsBarSmartViewAndModes = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.AlwaysExpandQsTileChunk -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            alwaysExpandQsTileChunk = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.AlwaysShowTimeDateOnQs -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            alwaysShowTimeDateOnQs = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.AddBrightnessProgressToQsBar -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            addBrightnessProgressToQsBar = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.AddVolumeProgressToQsBar -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            addVolumeProgressToQsBar = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.ShowTraditionalChineseDateOnQS -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            showTraditionalChineseDateOnQS = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.ModifyQSClockTextSize -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            modifyQSClockTextSize = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.QS.QSClockTextSize -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        qs = preference.systemUI.qs.copy(
                            qsClockTextSize = event.value
                        )
                    )
                )
            }
        }
    }
}

private fun SettingViewModel.onAODEvent(event: SystemUIEvent.AOD) {
    updateData { preference ->
        when (event) {
            is SystemUIEvent.AOD.HideAODStatusBar -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        aod = preference.systemUI.aod.copy(
                            hideAODStatusBar = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.AOD.AODLockSupportLunar -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        aod = preference.systemUI.aod.copy(
                            aodLockSupportLunar = event.value
                        )
                    )
                )
            }
        }
    }
}

private fun SettingViewModel.onNotification(event: SystemUIEvent.Notification) {
    updateData { preference ->
        when (event) {
            is SystemUIEvent.Notification.HideOngoingActivityMedia -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        notification = preference.systemUI.notification.copy(
                            hideOngoingActivityMedia = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.Notification.HideOngoingActivityMediaPackages -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        notification = preference.systemUI.notification.copy(
                            hideOngoingActivityMediaPackages = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.Notification.DisableNotificationGrouping -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        notification = preference.systemUI.notification.copy(
                            disableNotificationGrouping = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.Notification.AutoExpandNotifications -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        notification = preference.systemUI.notification.copy(
                            autoExpandNotifications = event.value
                        )
                    )
                )
            }
        }
    }
}

private fun SettingViewModel.onOtherEvent(event: SystemUIEvent.Other) {
    updateData { preference ->
        when (event) {
            is SystemUIEvent.Other.CustomPowerMenu -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        other = preference.systemUI.other.copy(
                            customPowerMenu = event.value
                        )
                    )
                )
            }

            is SystemUIEvent.Other.PowerMenuActions -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        other = preference.systemUI.other.copy(
                            powerMenuActions = PowerMenuAction.normalize(event.value)
                        )
                    )
                )
            }

            is SystemUIEvent.Other.DisableScreenshotCaptureSound -> {
                preference.copy(
                    systemUI = preference.systemUI.copy(
                        other = preference.systemUI.other.copy(
                            disableScreenshotCaptureSound = event.value
                        )
                    )
                )
            }
        }
    }
}
