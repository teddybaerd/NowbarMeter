// Modified by teddybaerd for the Nowbar Meter continuation (2026): independent app identity, localization, privacy controls and release maintenance.
package de.teddybaerd.nowbarmeter.ui.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import de.teddybaerd.nowbarmeter.BuildConfig
import de.teddybaerd.nowbarmeter.R
import de.teddybaerd.nowbarmeter.data.model.AppThemeMode
import de.teddybaerd.nowbarmeter.format.SpeedFormatter
import de.teddybaerd.nowbarmeter.ui.theme.CustomFontManager
import de.teddybaerd.nowbarmeter.ui.theme.PixelPulseTheme
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.LocalPreferenceTheme
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.PreferenceCategory
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SliderPreference
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.TextFieldPreference
import me.zhanghai.compose.preference.TwoTargetPreference
import java.util.Locale

class SettingsActivity : ComponentActivity() {
    private val viewModel by viewModels<SettingsViewModel>()
    private val updateViewModel by viewModels<de.teddybaerd.nowbarmeter.ui.MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isOledTheme by viewModel.isOledThemeEnabled.collectAsState(initial = false)
            val appThemeMode by viewModel.appThemeMode.collectAsState(initial = AppThemeMode.Dynamic.value)
            val appThemeColor by viewModel.appThemeColor.collectAsState(initial = AppThemeMode.DEFAULT_THEME_COLOR)
            PixelPulseTheme(
                isOledTheme = isOledTheme,
                themeMode = appThemeMode,
                themeColor = appThemeColor
            ) {
                SettingsScreen()
            }
        }
    }

    @Composable
    fun SettingsScreen() {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        DisposableEffect(lifecycle) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshOverlaySettings()
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.title_settings)) },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.content_description_back)
                            )
                        }
                    }
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            ProvidePreferenceLocals {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item { GeneralSection(viewModel) }
                    item { NotificationSection(viewModel) }
                    item { OverlaySection(viewModel) }
                    item { BackgroundSection(viewModel) }
                    item { AboutSection(updateViewModel) }
                    item {
                        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
        }
    }
}

@Composable
fun GeneralSection(viewModel: SettingsViewModel) {
    val view = LocalView.current
    val context = LocalContext.current
    val interval by viewModel.samplingInterval.collectAsState(initial = 1500L)
    val speedUnit by viewModel.speedUnit.collectAsState(initial = "0")
    val speedRateUnit by viewModel.speedRateUnit.collectAsState(initial = 0)
    val minSpeedUnit by viewModel.minSpeedUnit.collectAsState(initial = 0)
    val isHideKiloDecimalsEnabled by viewModel.isHideKiloDecimalsEnabled.collectAsState(initial = true)
    val isAutoStartEnabled by viewModel.isAutoStartServiceEnabled.collectAsState(initial = false)
    val isOledThemeEnabled by viewModel.isOledThemeEnabled.collectAsState(initial = false)
    val appThemeMode by viewModel.appThemeMode.collectAsState(initial = AppThemeMode.Dynamic.value)
    val appThemeColor by viewModel.appThemeColor.collectAsState(initial = AppThemeMode.DEFAULT_THEME_COLOR)
    val fontPreset by viewModel.fontPreset.collectAsState(initial = 0)
    val customFontName by viewModel.customFontName.collectAsState(initial = "")
    val canEnableAutoStart by viewModel.canEnableAutoStart.collectAsState()
    val hasOverlayPermission by viewModel.canOverlay.collectAsState()
    val hasNotificationPermission by viewModel.hasNotificationPermission.collectAsState()

    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importCustomFont(uri)
        }
    }

    PreferenceCategory(title = { Text(stringResource(R.string.settings_category_general)) })

    SliderPreference(
        value = 0F,
        onValueChange = { },
        sliderValue = interval.toFloat(),
        onSliderValueChange = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            viewModel.setSamplingInterval(it.toLong())
        },
        valueRange = 1000f..3000f,
        valueSteps = 19,
        title = { Text(stringResource(R.string.settings_sampling_interval)) },
        summary = { Text(stringResource(R.string.settings_sampling_interval_desc)) },
        valueText = { Text("${interval}ms") }
    )

    val labelRateBytes = stringResource(R.string.settings_speed_rate_unit_bytes)
    val labelRateBits = stringResource(R.string.settings_speed_rate_unit_bits)
    val rateUnitLabel = if (speedRateUnit == 1) labelRateBits else labelRateBytes
    ListPreference(
        value = rateUnitLabel,
        onValueChange = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            viewModel.setSpeedRateUnit(if (it == labelRateBits) 1 else 0)
        },
        title = { Text(stringResource(R.string.settings_speed_rate_unit_title)) },
        values = listOf(labelRateBytes, labelRateBits),
        summary = {
            Column {
                Text(rateUnitLabel)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.settings_speed_rate_unit_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )

    SpeedUnitPreference(
        currentValue = speedUnit,
        rateUnit = speedRateUnit,
        onValueChange = { viewModel.setSpeedUnit(it) }
    )

    val selectedUnits = remember(speedUnit) {
        speedUnit.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }
    val isAutoSpeedUnit = selectedUnits.isEmpty() || selectedUnits.contains(0)

    val labelMinNone = stringResource(R.string.settings_min_speed_unit_none)
    val labelMinKilo = SpeedFormatter.fullUnit(2, speedRateUnit)
    val labelMinMega = SpeedFormatter.fullUnit(3, speedRateUnit)
    val labelMinGiga = SpeedFormatter.fullUnit(4, speedRateUnit)
    val minUnitLabel = when (minSpeedUnit) {
        1 -> labelMinKilo
        2 -> labelMinMega
        3 -> labelMinGiga
        else -> labelMinNone
    }
    ListPreference(
        value = minUnitLabel,
        onValueChange = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val newMin = when (it) {
                labelMinKilo -> 1
                labelMinMega -> 2
                labelMinGiga -> 3
                else -> 0
            }
            viewModel.setMinSpeedUnit(newMin)
        },
        enabled = isAutoSpeedUnit,
        title = { Text(stringResource(R.string.settings_min_speed_unit_title)) },
        values = listOf(labelMinNone, labelMinKilo, labelMinMega, labelMinGiga),
        summary = { Text("$minUnitLabel — ${stringResource(R.string.settings_min_speed_unit_desc)}") }
    )

    SwitchPreference(
        value = isHideKiloDecimalsEnabled,
        onValueChange = { isChecked ->
            performToggleHaptic(view, isChecked)
            viewModel.setHideKiloDecimalsEnabled(isChecked)
        },
        title = { Text(stringResource(R.string.settings_hide_kilo_decimals_title)) },
        summary = { Text(stringResource(R.string.settings_hide_kilo_decimals_desc)) }
    )

    // Font Presets & Custom Font Picker (Issue #2)
    val fontDefault = stringResource(R.string.settings_font_preset_default)
    val fontCondensed = stringResource(R.string.settings_font_preset_condensed)
    val fontMonospace = stringResource(R.string.settings_font_preset_monospace)
    val fontSerif = stringResource(R.string.settings_font_preset_serif)
    val fontCustom = stringResource(R.string.settings_font_preset_custom)
    val fontPresetLabel = when (fontPreset) {
        CustomFontManager.PRESET_CONDENSED -> fontCondensed
        CustomFontManager.PRESET_MONOSPACE -> fontMonospace
        CustomFontManager.PRESET_SERIF -> fontSerif
        CustomFontManager.PRESET_CUSTOM -> fontCustom
        else -> fontDefault
    }
    ListPreference(
        value = fontPresetLabel,
        onValueChange = { selected ->
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            when (selected) {
                fontCondensed -> viewModel.setFontPreset(CustomFontManager.PRESET_CONDENSED)
                fontMonospace -> viewModel.setFontPreset(CustomFontManager.PRESET_MONOSPACE)
                fontSerif -> viewModel.setFontPreset(CustomFontManager.PRESET_SERIF)
                fontCustom -> {
                    if (customFontName.isBlank()) {
                        fontPickerLauncher.launch(arrayOf("font/*", "application/octet-stream", "*/*"))
                    } else {
                        viewModel.setFontPreset(CustomFontManager.PRESET_CUSTOM)
                    }
                }
                else -> viewModel.setFontPreset(CustomFontManager.PRESET_DEFAULT)
            }
        },
        title = { Text(stringResource(R.string.settings_font_preset_title)) },
        values = listOf(fontDefault, fontCondensed, fontMonospace, fontSerif, fontCustom),
        summary = { Text(fontPresetLabel) }
    )

    Preference(
        title = { Text(stringResource(R.string.settings_import_custom_font_title)) },
        summary = {
            Text(
                if (customFontName.isNotBlank()) {
                    stringResource(R.string.settings_import_custom_font_loaded, customFontName)
                } else {
                    stringResource(R.string.settings_import_custom_font_desc)
                }
            )
        },
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            fontPickerLauncher.launch(arrayOf("font/*", "application/octet-stream", "*/*"))
        }
    )

    val labelThemeDynamic = stringResource(R.string.settings_theme_mode_dynamic)
    val labelThemeFixed = stringResource(R.string.settings_theme_mode_fixed)
    val currentThemeLabel =
        if (AppThemeMode.fromValue(appThemeMode) == AppThemeMode.Fixed) labelThemeFixed else labelThemeDynamic
    ListPreference(
        value = currentThemeLabel,
        onValueChange = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val mode = if (it == labelThemeFixed) AppThemeMode.Fixed.value else AppThemeMode.Dynamic.value
            viewModel.setAppThemeMode(mode)
        },
        title = { Text(stringResource(R.string.settings_theme_mode_title)) },
        values = listOf(labelThemeDynamic, labelThemeFixed),
        summary = { Text(currentThemeLabel) }
    )

    if (AppThemeMode.fromValue(appThemeMode) == AppThemeMode.Fixed) {
        ColorPreference(
            title = stringResource(R.string.settings_theme_color_title),
            color = Color(appThemeColor),
            showAlpha = false,
            onColorSelected = { viewModel.setAppThemeColor(it.toArgb()) }
        )
    }

    SwitchPreference(
        value = isOledThemeEnabled,
        onValueChange = { isChecked ->
            performToggleHaptic(view, isChecked)
            viewModel.setOledThemeEnabled(isChecked)
        },
        title = { Text(stringResource(R.string.settings_oled_theme_title)) },
        summary = { Text(stringResource(R.string.settings_oled_theme_desc)) }
    )

    val autoStartSummary = if (canEnableAutoStart) {
        stringResource(R.string.settings_auto_start_service_desc)
    } else {
        stringResource(R.string.settings_auto_start_disabled_reason)
    }

    SwitchPreference(
        value = isAutoStartEnabled,
        onValueChange = { isChecked ->
            performToggleHaptic(view, isChecked)
            viewModel.setAutoStartServiceEnabled(isChecked)
        },
        enabled = canEnableAutoStart,
        title = { Text(stringResource(R.string.settings_auto_start_service_title)) },
        summary = { Text(autoStartSummary) }
    )

    val overlayPermissionSummary = if (hasOverlayPermission) {
        stringResource(R.string.settings_permission_granted)
    } else {
        stringResource(R.string.settings_permission_denied)
    }
    Preference(
        title = { Text(stringResource(R.string.settings_permission_overlay)) },
        summary = { Text(overlayPermissionSummary) },
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = "package:${context.packageName}".toUri()
            context.startActivity(intent)
        }
    )

    val notificationPermissionSummary = if (hasNotificationPermission) {
        stringResource(R.string.settings_permission_granted)
    } else {
        stringResource(R.string.settings_permission_denied)
    }
    Preference(
        title = { Text(stringResource(R.string.settings_permission_notification)) },
        summary = { Text(notificationPermissionSummary) },
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            context.startActivity(intent)
        }
    )
}

@Composable
fun SpeedUnitPreference(
    currentValue: String,
    rateUnit: Int,
    onValueChange: (String) -> Unit
) {
    val view = LocalView.current
    val selectedUnits = remember(currentValue) {
        currentValue.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    var showDialog by remember { mutableStateOf(false) }

    val labelAuto = stringResource(R.string.settings_speed_unit_auto)
    val units = listOf(
        0 to labelAuto,
        1 to SpeedFormatter.fullUnit(1, rateUnit),
        2 to SpeedFormatter.fullUnit(2, rateUnit),
        3 to SpeedFormatter.fullUnit(3, rateUnit),
        4 to SpeedFormatter.fullUnit(4, rateUnit)
    )

    val summary = if (selectedUnits.isEmpty() || selectedUnits.contains(0)) {
        labelAuto
    } else {
        units.filter { it.first in selectedUnits }.joinToString(", ") { it.second }
    }

    Preference(
        title = { Text(stringResource(R.string.settings_speed_unit_title)) },
        summary = { Text(summary) },
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            showDialog = true
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.settings_speed_unit_title)) },
            text = {
                Column {
                    units.forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = selectedUnits.contains(value),
                                onCheckedChange = { checked ->
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    val newSet = if (checked) {
                                        if (value == 0) setOf(0) else (selectedUnits - 0) + value
                                    } else {
                                        selectedUnits - value
                                    }
                                    onValueChange(newSet.sorted().joinToString(","))
                                }
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    showDialog = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }
}

@Composable
fun BackgroundSection(viewModel: SettingsViewModel) {
    val view = LocalView.current
    val context = LocalContext.current
    val isIgnoringBatteryOptimizations by viewModel.isIgnoringBatteryOptimizations.collectAsState()
    val isHideFromRecents by viewModel.isHideFromRecents.collectAsState(initial = false)

    PreferenceCategory(title = { Text(stringResource(R.string.settings_category_background)) })

    Preference(
        title = { Text(stringResource(R.string.settings_ignore_battery_optimizations_title)) },
        summary = {
            Text(
                if (isIgnoringBatteryOptimizations) stringResource(R.string.settings_ignore_battery_optimizations_on)
                else stringResource(R.string.settings_ignore_battery_optimizations_off)
            )
        },
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = "package:${context.packageName}".toUri()
            context.startActivity(intent)
        }
    )

    SwitchPreference(
        value = isHideFromRecents,
        onValueChange = { isChecked ->
            performToggleHaptic(view, isChecked)
            viewModel.setHideFromRecents(isChecked)
        },
        title = { Text(stringResource(R.string.settings_hide_from_recents_title)) },
        summary = { Text(stringResource(R.string.settings_hide_from_recents_desc)) }
    )

    Preference(
        title = { Text(stringResource(R.string.settings_dont_kill_my_app_title)) },
        summary = { Text(stringResource(R.string.settings_dont_kill_my_app_desc)) },
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val url = "https://dontkillmyapp.com/"
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, url.toUri())
        }
    )
}

@Composable
fun OverlaySection(viewModel: SettingsViewModel) {
    val view = LocalView.current
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    val canOverlay by viewModel.canOverlay.collectAsState()

    val isEnabled by viewModel.isOverlayEnabled.collectAsState(initial = false)
    val isLocked by viewModel.isOverlayLocked.collectAsState(initial = false)
    val isShowOnStatusBar by viewModel.isOverlayShowOnStatusBar.collectAsState(initial = false)
    val isPortraitOnly by viewModel.isOverlayPortraitOnly.collectAsState(initial = false)
    val isHideInImmersiveMode by viewModel.isOverlayHideInImmersiveMode.collectAsState(initial = false)
    val autoHideThreshold by viewModel.overlayAutoHideThreshold.collectAsState(initial = 0L)
    val isHideBackground by viewModel.isOverlayHideBackground.collectAsState(initial = false)
    val overlayX by viewModel.overlayX.collectAsState(initial = 100)
    val overlayY by viewModel.overlayY.collectAsState(initial = 200)
    val isOverlayUseDefaultColors by viewModel.isOverlayUseDefaultColors.collectAsState(initial = false)
    val bgColor by viewModel.overlayBgColor.collectAsState(initial = 0)
    val textColor by viewModel.overlayTextColor.collectAsState(initial = 0)
    val cornerRadius by viewModel.overlayCornerRadius.collectAsState(initial = 8)
    val padding by viewModel.overlayPadding.collectAsState(initial = 8)
    val textSize by viewModel.overlayTextSize.collectAsState(initial = 10f)
    val textUp by viewModel.overlayTextUp.collectAsState(initial = "▲ ")
    val textDown by viewModel.overlayTextDown.collectAsState(initial = "▼ ")
    val upFirst by viewModel.overlayOrderUpFirst.collectAsState(initial = false)
    val direction by viewModel.overlayDirection.collectAsState(initial = 0)
    val displayMode by viewModel.overlayDisplayMode.collectAsState(initial = 0)
    val alignment by viewModel.overlayAlignment.collectAsState(initial = 0)
    val meterSpacing by viewModel.overlayMeterSpacing.collectAsState(initial = 8)
    val speedRateUnit by viewModel.speedRateUnit.collectAsState(initial = 0)

    PreferenceCategory(title = { Text(stringResource(R.string.settings_category_overlay)) })
    val isSwitchEnabled = !isServiceRunning || canOverlay
    val summaryText = if (isSwitchEnabled) {
        stringResource(R.string.config_enable_overlay_desc)
    } else {
        stringResource(R.string.config_overlay_disabled_reason)
    }
    SwitchPreference(
        value = isEnabled,
        onValueChange = { isChecked ->
            performToggleHaptic(view, isChecked)
            viewModel.setOverlayEnabled(isChecked)
        },
        enabled = isSwitchEnabled,
        title = { Text(stringResource(R.string.config_enable_overlay)) },
        summary = { Text(summaryText) }
    )

    if (isEnabled) {
        SwitchPreference(
            value = isLocked,
            onValueChange = { isChecked ->
                performToggleHaptic(view, isChecked)
                viewModel.setOverlayLocked(isChecked)
            },
            title = { Text(stringResource(R.string.settings_lock_overlay)) },
            summary = { Text(stringResource(R.string.config_lock_overlay_desc)) }
        )
        SwitchPreference(
            value = isShowOnStatusBar,
            onValueChange = { isChecked ->
                performToggleHaptic(view, isChecked)
                viewModel.setOverlayShowOnStatusBar(isChecked)
            },
            title = { Text(stringResource(R.string.settings_overlay_show_on_status_bar)) },
            summary = { Text(stringResource(R.string.settings_overlay_show_on_status_bar_desc)) }
        )
        TextFieldPreference(
            value = overlayX.toString(),
            onValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                it.trim().toIntOrNull()?.let { x -> viewModel.setOverlayX(x) }
            },
            textToValue = { it },
            title = { Text(stringResource(R.string.settings_overlay_position_x)) },
            summary = { Text("${overlayX}px — ${stringResource(R.string.settings_overlay_position_x_desc)}") }
        )
        TextFieldPreference(
            value = overlayY.toString(),
            onValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                it.trim().toIntOrNull()?.let { y -> viewModel.setOverlayY(y) }
            },
            textToValue = { it },
            title = { Text(stringResource(R.string.settings_overlay_position_y)) },
            summary = { Text("${overlayY}px — ${stringResource(R.string.settings_overlay_position_y_desc)}") }
        )
        SwitchPreference(
            value = isPortraitOnly,
            onValueChange = { isChecked ->
                performToggleHaptic(view, isChecked)
                viewModel.setOverlayPortraitOnly(isChecked)
            },
            title = { Text(stringResource(R.string.settings_overlay_portrait_only)) },
            summary = { Text(stringResource(R.string.settings_overlay_portrait_only_desc)) }
        )
        SwitchPreference(
            value = isHideInImmersiveMode,
            onValueChange = { isChecked ->
                performToggleHaptic(view, isChecked)
                viewModel.setOverlayHideInImmersiveMode(isChecked)
            },
            title = { Text(stringResource(R.string.settings_overlay_hide_in_immersive_mode)) },
            summary = { Text(stringResource(R.string.settings_overlay_hide_in_immersive_mode_desc)) }
        )

        val kiloUnitLabel = SpeedFormatter.fullUnit(2, speedRateUnit)
        SliderPreference(
            value = 0F,
            onValueChange = { },
            sliderValue = autoHideThreshold.toFloat() / 1024f,
            onSliderValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.setOverlayAutoHideThreshold((it * 1024f).toLong())
            },
            valueRange = 0f..1024f,
            valueSteps = 20,
            title = { Text(stringResource(R.string.settings_overlay_auto_hide_threshold)) },
            summary = {
                if (autoHideThreshold <= 0L) {
                    Text(stringResource(R.string.settings_overlay_auto_hide_threshold_disabled))
                } else {
                    val thresholdStr = SpeedFormatter.formatSpeedLine(
                        bytes = autoHideThreshold,
                        rateUnit = speedRateUnit
                    )
                    Text(stringResource(R.string.settings_overlay_auto_hide_threshold_desc, thresholdStr))
                }
            },
            valueText = {
                Text(
                    SpeedFormatter.formatSpeedLine(
                        bytes = autoHideThreshold,
                        rateUnit = speedRateUnit
                    )
                )
            }
        )
        TextFieldPreference(
            value = SpeedFormatter.formatThresholdInputValue(autoHideThreshold, speedRateUnit),
            onValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                SpeedFormatter.parseThresholdInputValue(it, speedRateUnit)?.let { bytes ->
                    viewModel.setOverlayAutoHideThreshold(bytes)
                }
            },
            title = { Text(stringResource(R.string.settings_overlay_auto_hide_threshold_input_title, kiloUnitLabel)) },
            summary = { Text(stringResource(R.string.settings_overlay_auto_hide_threshold_input_summary, kiloUnitLabel)) },
            textToValue = { it }
        )

        SwitchPreference(
            value = isOverlayUseDefaultColors,
            onValueChange = { isChecked ->
                performToggleHaptic(view, isChecked)
                viewModel.setOverlayUseDefaultColors(isChecked)
            },
            title = { Text(stringResource(R.string.settings_overlay_use_default_colors)) },
            summary = { Text(stringResource(R.string.settings_overlay_use_default_colors_desc)) }
        )
        SwitchPreference(
            value = isHideBackground,
            onValueChange = { isChecked ->
                performToggleHaptic(view, isChecked)
                viewModel.setOverlayHideBackground(isChecked)
            },
            title = { Text(stringResource(R.string.settings_overlay_hide_background)) },
            summary = { Text(stringResource(R.string.settings_overlay_hide_background_desc)) }
        )
        ColorPreference(
            title = stringResource(R.string.settings_overlay_bg_color),
            color = Color(bgColor),
            enabled = !isOverlayUseDefaultColors && !isHideBackground,
            onColorSelected = { viewModel.setOverlayBgColor(it.toArgb()) }
        )
        ColorPreference(
            title = stringResource(R.string.settings_overlay_text_color),
            color = Color(textColor),
            enabled = !isOverlayUseDefaultColors,
            onColorSelected = { viewModel.setOverlayTextColor(it.toArgb()) }
        )
        SliderPreference(
            value = 0F,
            onValueChange = { },
            sliderValue = cornerRadius.toFloat(),
            onSliderValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.setOverlayCornerRadius(it.toInt())
            },
            valueRange = 0f..32f,
            valueSteps = 32,
            title = { Text(stringResource(R.string.settings_overlay_corner_radius)) },
            valueText = { Text("${cornerRadius}dp") }
        )
        SliderPreference(
            value = 0F,
            onValueChange = { },
            sliderValue = padding.toFloat(),
            onSliderValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.setOverlayPadding(it.toInt())
            },
            valueRange = 0f..16f,
            valueSteps = 16,
            title = { Text(stringResource(R.string.settings_overlay_padding)) },
            valueText = { Text("${padding}dp") }
        )
        SliderPreference(
            value = 0F,
            onValueChange = { },
            sliderValue = textSize,
            onSliderValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.setOverlayTextSize(it)
            },
            valueRange = 8f..24f,
            title = { Text(stringResource(R.string.settings_overlay_text_size)) },
            valueText = { Text("${"%.1f".format(Locale.getDefault(), textSize)}sp") }
        )

        val labelBoth = stringResource(R.string.settings_display_mode_both)
        val labelUpload = stringResource(R.string.settings_display_mode_upload)
        val labelDownload = stringResource(R.string.settings_display_mode_download)
        val labelTotal = stringResource(R.string.settings_display_mode_total)
        val overlayDisplayLabel = when (displayMode) {
            1 -> labelUpload
            2 -> labelDownload
            3 -> labelTotal
            else -> labelBoth
        }
        ListPreference(
            value = overlayDisplayLabel,
            onValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                val mode = when (it) {
                    labelUpload -> 1
                    labelDownload -> 2
                    labelTotal -> 3
                    else -> 0
                }
                viewModel.setOverlayDisplayMode(mode)
            },
            title = { Text(stringResource(R.string.settings_overlay_display_mode)) },
            values = listOf(labelBoth, labelUpload, labelDownload, labelTotal),
            summary = { Text(overlayDisplayLabel) }
        )

        TextFieldPreference(
            value = textUp,
            onValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.setOverlayTextUp(it)
            },
            textToValue = { it },
            title = { Text(stringResource(R.string.settings_text_prefix_up)) },
            summary = { Text(stringResource(R.string.settings_text_prefix_up_desc, textUp)) },
        )
        TextFieldPreference(
            value = textDown,
            onValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.setOverlayTextDown(it)
            },
            textToValue = { it },
            title = { Text(stringResource(R.string.settings_text_prefix_down)) },
            summary = { Text(stringResource(R.string.settings_text_prefix_down_desc, textDown)) },
        )

        if (displayMode == 0) {
            SwitchPreference(
                value = upFirst,
                onValueChange = { isChecked ->
                    performToggleHaptic(view, isChecked)
                    viewModel.setOverlayOrderUpFirst(isChecked)
                },
                title = { Text(stringResource(R.string.settings_show_up_first)) },
                summary = { Text(stringResource(R.string.settings_show_up_first_desc)) }
            )

            val labelHorizontal = stringResource(R.string.settings_overlay_direction_horizontal)
            val labelVertical = stringResource(R.string.settings_overlay_direction_vertical)
            val directionLabel = if (direction == 1) labelVertical else labelHorizontal
            ListPreference(
                value = directionLabel,
                onValueChange = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    viewModel.setOverlayDirection(if (it == labelVertical) 1 else 0)
                },
                title = { Text(stringResource(R.string.settings_overlay_direction)) },
                values = listOf(labelHorizontal, labelVertical),
                summary = { Text(directionLabel) }
            )

            if (direction == 0) {
                SliderPreference(
                    value = 0F,
                    onValueChange = { },
                    sliderValue = meterSpacing.toFloat(),
                    onSliderValueChange = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        viewModel.setOverlayMeterSpacing(it.toInt())
                    },
                    valueRange = 0f..24f,
                    valueSteps = 24,
                    title = { Text(stringResource(R.string.settings_overlay_meter_spacing)) },
                    summary = { Text(stringResource(R.string.settings_overlay_meter_spacing_desc)) },
                    valueText = { Text("${meterSpacing}dp") }
                )
            } else {
                val alignStart = stringResource(R.string.settings_overlay_alignment_start)
                val alignCenter = stringResource(R.string.settings_overlay_alignment_center)
                val alignEnd = stringResource(R.string.settings_overlay_alignment_end)
                val alignLabel = when (alignment) {
                    1 -> alignCenter
                    2 -> alignEnd
                    else -> alignStart
                }
                ListPreference(
                    value = alignLabel,
                    onValueChange = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        val newAlign = when (it) {
                            alignCenter -> 1
                            alignEnd -> 2
                            else -> 0
                        }
                        viewModel.setOverlayAlignment(newAlign)
                    },
                    title = { Text(stringResource(R.string.settings_overlay_alignment)) },
                    values = listOf(alignStart, alignCenter, alignEnd),
                    summary = { Text(alignLabel) }
                )
            }
        }
    }
}

@Composable
fun NotificationSection(viewModel: SettingsViewModel) {
    val view = LocalView.current
    val isEnabled by viewModel.isNotificationEnabled.collectAsState(initial = true)
    val isLiveUpdateEnabled by viewModel.isLiveUpdateEnabled.collectAsState(initial = false)
    val isShowOnLockscreenEnabled by viewModel.isShowOnLockscreenEnabled.collectAsState(initial = false)
    val textUp by viewModel.notificationTextUp.collectAsState(initial = "▲ ")
    val textDown by viewModel.notificationTextDown.collectAsState(initial = "▼ ")
    val upFirst by viewModel.notificationOrderUpFirst.collectAsState(initial = false)
    val displayMode by viewModel.notificationDisplayMode.collectAsState(initial = 0)
    val iconMode by viewModel.notificationIconMode.collectAsState(initial = 0)
    val nowbarShowArrows by viewModel.nowbarShowArrows.collectAsState(initial = false)
    val nowbarExpandedMode by viewModel.nowbarExpandedMode.collectAsState(initial = 0)
    val textSize by viewModel.notificationTextSize.collectAsState(initial = 0.60f)
    val unitSize by viewModel.notificationUnitSize.collectAsState(initial = 0.45f)
    val isCompactSpeedTextEnabled by viewModel.isCompactSpeedTextEnabled.collectAsState(initial = true)
    val isBlankNotificationEnabled by viewModel.isBlankNotificationEnabled.collectAsState(initial = false)
    val isTransparentIconEnabled by viewModel.isNotificationTransparentIconEnabled.collectAsState(initial = false)
    val speedRateUnit by viewModel.speedRateUnit.collectAsState(initial = 0)

    PreferenceCategory(title = { Text(stringResource(R.string.settings_category_notification)) })
    SwitchPreference(
        value = isEnabled,
        onValueChange = { isChecked ->
            performToggleHaptic(view, isChecked)
            viewModel.setNotificationEnabled(isChecked)
        },
        title = { Text(stringResource(R.string.config_enable_notification)) },
        summary = { Text(stringResource(R.string.config_enable_notification_desc)) }
    )

    if (isEnabled) {
        SwitchPreference(
            value = isLiveUpdateEnabled,
            onValueChange = { isChecked ->
                performToggleHaptic(view, isChecked)
                viewModel.setLiveUpdateEnabled(isChecked)
            },
            enabled = !isBlankNotificationEnabled,
            title = { Text(stringResource(R.string.config_enable_live_update)) },
            summary = { Text(stringResource(R.string.config_enable_live_update_desc)) }
        )
        SwitchPreference(
            value = isShowOnLockscreenEnabled,
            onValueChange = { isChecked ->
                performToggleHaptic(view, isChecked)
                viewModel.setShowOnLockscreenEnabled(isChecked)
            },
            title = { Text(stringResource(R.string.settings_show_on_lockscreen_title)) },
            summary = { Text(stringResource(R.string.settings_show_on_lockscreen_desc)) }
        )

        val labelTotal = stringResource(R.string.settings_display_mode_total)
        val labelUpload = stringResource(R.string.settings_display_mode_upload)
        val labelDownload = stringResource(R.string.settings_display_mode_download)
        val labelDualPill = stringResource(R.string.settings_nowbar_pill_dual)
        val labelBoth = stringResource(R.string.settings_display_mode_both)

        val iconModeLabel = when (iconMode) {
            1 -> labelUpload
            2 -> labelDownload
            3 -> labelDualPill
            else -> labelTotal
        }
        ListPreference(
            value = iconModeLabel,
            onValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                val mode = when (it) {
                    labelUpload -> 1
                    labelDownload -> 2
                    labelDualPill -> 3
                    else -> 0
                }
                viewModel.setNotificationIconMode(mode)
            },
            title = { Text(stringResource(R.string.settings_notification_icon_mode)) },
            values = listOf(labelTotal, labelDownload, labelUpload, labelDualPill),
            summary = { Text(iconModeLabel) }
        )

        if ((isLiveUpdateEnabled || isShowOnLockscreenEnabled) && (iconMode == 1 || iconMode == 2)) {
            SwitchPreference(
                value = nowbarShowArrows,
                onValueChange = { isChecked ->
                    performToggleHaptic(view, isChecked)
                    viewModel.setNowbarShowArrows(isChecked)
                },
                title = { Text(stringResource(R.string.settings_nowbar_show_arrows_title)) },
                summary = { Text(stringResource(R.string.settings_nowbar_show_arrows_desc)) }
            )
        }

        if (isLiveUpdateEnabled || isShowOnLockscreenEnabled) {
            val expDual = stringResource(R.string.settings_nowbar_expanded_dual)
            val expTotal = stringResource(R.string.settings_display_mode_total)
            val expFollow = stringResource(R.string.settings_nowbar_expanded_follow)
            val expLabel = when (nowbarExpandedMode) {
                1 -> expTotal
                2 -> expFollow
                else -> expDual
            }
            ListPreference(
                value = expLabel,
                onValueChange = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    val mode = when (it) {
                        expTotal -> 1
                        expFollow -> 2
                        else -> 0
                    }
                    viewModel.setNowbarExpandedMode(mode)
                },
                title = { Text(stringResource(R.string.settings_nowbar_expanded_mode_title)) },
                values = listOf(expDual, expTotal, expFollow),
                summary = { Text(expLabel) }
            )
        }

        SwitchPreference(
            value = isCompactSpeedTextEnabled,
            onValueChange = { isChecked ->
                performToggleHaptic(view, isChecked)
                viewModel.setCompactSpeedTextEnabled(isChecked)
            },
            title = { Text(stringResource(R.string.settings_compact_speed_text_title)) },
            summary = { Text(stringResource(R.string.settings_compact_speed_text_desc)) }
        )
        SwitchPreference(
            value = isBlankNotificationEnabled,
            onValueChange = { isChecked ->
                performToggleHaptic(view, isChecked)
                viewModel.setBlankNotificationEnabled(isChecked)
            },
            enabled = !isLiveUpdateEnabled,
            title = { Text(stringResource(R.string.settings_blank_notification_title)) },
            summary = { Text(stringResource(R.string.settings_blank_notification_desc)) }
        )
        TextFieldPreference(
            value = textUp,
            onValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.setNotificationTextUp(it)
            },
            textToValue = { it },
            title = { Text(stringResource(R.string.settings_text_prefix_up)) },
            summary = { Text(stringResource(R.string.settings_text_prefix_up_desc, textUp)) },
        )
        TextFieldPreference(
            value = textDown,
            onValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.setNotificationTextDown(it)
            },
            textToValue = { it },
            title = { Text(stringResource(R.string.settings_text_prefix_down)) },
            summary = { Text(stringResource(R.string.settings_text_prefix_down_desc, textDown)) },
        )
        SwitchPreference(
            value = upFirst,
            onValueChange = { isChecked ->
                performToggleHaptic(view, isChecked)
                viewModel.setNotificationOrderUpFirst(isChecked)
            },
            title = { Text(stringResource(R.string.settings_show_up_first)) },
            summary = { Text(stringResource(R.string.settings_show_up_first_desc)) }
        )

        val displayModeLabel = when (displayMode) {
            1 -> labelUpload
            2 -> labelDownload
            else -> labelBoth
        }
        ListPreference(
            value = displayModeLabel,
            onValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                val mode = when (it) {
                    labelUpload -> 1
                    labelDownload -> 2
                    else -> 0
                }
                viewModel.setNotificationDisplayMode(mode)
            },
            title = { Text(stringResource(R.string.settings_notification_display_mode)) },
            values = listOf(
                labelBoth,
                labelUpload,
                labelDownload
            ),
            summary = { Text(displayModeLabel) }
        )

        SliderPreference(
            enabled = !isLiveUpdateEnabled,
            value = 0F,
            onValueChange = { },
            sliderValue = textSize,
            onSliderValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.setNotificationTextSize(it)
            },
            valueRange = 0.1f..1.0f,
            title = { Text(stringResource(R.string.settings_notification_text_size)) },
            valueText = { Text("%.2f".format(textSize)) }
        )

        SliderPreference(
            enabled = !isLiveUpdateEnabled,
            value = 0F,
            onValueChange = { },
            sliderValue = unitSize,
            onSliderValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.setNotificationUnitSize(it)
            },
            valueRange = 0.1f..1.0f,
            title = { Text(stringResource(R.string.settings_notification_unit_size)) },
            valueText = { Text("%.2f".format(unitSize)) }
        )

        val threshold by viewModel.notificationThreshold.collectAsState(initial = 0L)
        val lowTrafficMode by viewModel.notificationLowTrafficMode.collectAsState(initial = 0)
        val kiloUnitLabel = SpeedFormatter.fullUnit(2, speedRateUnit)

        SliderPreference(
            value = 0F,
            onValueChange = { },
            sliderValue = threshold.toFloat() / 1024f,
            onSliderValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.setNotificationThreshold((it * 1024f).toLong())
            },
            valueRange = 0f..1024f,
            valueSteps = 20,
            title = { Text(stringResource(R.string.settings_notification_threshold)) },
            summary = {
                if (threshold == 0L) {
                    Text(stringResource(R.string.settings_notification_threshold_disabled))
                } else {
                    val thresholdStr = SpeedFormatter.formatSpeedLine(
                        bytes = threshold,
                        rateUnit = speedRateUnit
                    )
                    Text(
                        stringResource(
                            R.string.settings_notification_threshold_desc,
                            thresholdStr
                        )
                    )
                }
            },
            valueText = {
                Text(
                    SpeedFormatter.formatSpeedLine(
                        bytes = threshold,
                        rateUnit = speedRateUnit
                    )
                )
            }
        )

        TextFieldPreference(
            value = SpeedFormatter.formatThresholdInputValue(threshold, speedRateUnit),
            onValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                SpeedFormatter.parseThresholdInputValue(it, speedRateUnit)?.let { bytes ->
                    viewModel.setNotificationThreshold(bytes)
                }
            },
            title = { Text(stringResource(R.string.settings_notification_threshold_input_title, kiloUnitLabel)) },
            summary = { Text(stringResource(R.string.settings_notification_threshold_input_summary, kiloUnitLabel)) },
            textToValue = { it },
        )

        val labelStatic = stringResource(R.string.settings_notification_low_traffic_mode_static)
        val labelDynamic = stringResource(R.string.settings_notification_low_traffic_mode_dynamic)
        val lowTrafficModeLabel = when (lowTrafficMode) {
            1 -> labelDynamic
            else -> labelStatic
        }

        ListPreference(
            value = lowTrafficModeLabel,
            onValueChange = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                val mode = when (it) {
                    labelDynamic -> 1
                    else -> 0
                }
                viewModel.setNotificationLowTrafficMode(mode)
            },
            title = { Text(stringResource(R.string.settings_notification_low_traffic_mode)) },
            values = listOf(
                labelStatic,
                labelDynamic
            ),
            summary = {
                Column {
                    Text(lowTrafficModeLabel)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_notification_low_traffic_mode_explanation),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        val useCustomColor by viewModel.notificationUseCustomColor.collectAsState(initial = false)
        val notificationColor by viewModel.notificationColor.collectAsState(initial = 0)

        SwitchPreference(
            value = isTransparentIconEnabled,
            onValueChange = { isChecked ->
                performToggleHaptic(view, isChecked)
                viewModel.setNotificationTransparentIconEnabled(isChecked)
            },
            enabled = !useCustomColor,
            title = { Text(stringResource(R.string.settings_notification_transparent_icon_title)) },
            summary = { Text(stringResource(R.string.settings_notification_transparent_icon_desc)) }
        )

        SwitchPreference(
            value = useCustomColor,
            onValueChange = { isChecked ->
                performToggleHaptic(view, isChecked)
                viewModel.setNotificationUseCustomColor(isChecked)
            },
            enabled = !isTransparentIconEnabled,
            title = { Text(stringResource(R.string.settings_notification_use_custom_color_title)) },
            summary = { Text(stringResource(R.string.settings_notification_use_custom_color_desc)) }
        )

        ColorPreference(
            title = stringResource(R.string.settings_notification_color_title),
            color = Color(notificationColor),
            enabled = useCustomColor && !isTransparentIconEnabled,
            showAlpha = false,
            onColorSelected = { viewModel.setNotificationColor(it.toArgb()) }
        )
    }
}

@Composable
fun AboutSection(updateViewModel: de.teddybaerd.nowbarmeter.ui.MainViewModel) {
    val context = LocalContext.current
    val automaticChecks by updateViewModel.automaticUpdateChecks.collectAsState(initial = null)
    val updateStatus by updateViewModel.updateStatus.collectAsState()
    de.teddybaerd.nowbarmeter.ui.UpdateDialog(updateViewModel)
    val view = LocalView.current
    val uriHandler = LocalUriHandler.current
    PreferenceCategory(title = { Text(stringResource(R.string.settings_category_about)) })
    Preference(
        title = { Text(stringResource(R.string.settings_app_version)) },
        summary = { Text(BuildConfig.VERSION_NAME) }
    )
    SwitchPreference(
        value = automaticChecks == true,
        enabled = automaticChecks != null,
        onValueChange = { updateViewModel.setAutomaticUpdateChecks(it) },
        title = { Text(stringResource(R.string.settings_auto_updates)) },
        summary = { Text(stringResource(R.string.settings_auto_updates_desc)) }
    )
    Preference(
        title = { Text(stringResource(R.string.settings_check_updates)) },
        summary = { Text(stringResource(updateStatus ?: R.string.settings_check_updates_desc)) },
        enabled = updateStatus != R.string.update_checking,
        onClick = { updateViewModel.checkManually() }
    )
    Preference(
        title = { Text(stringResource(R.string.settings_licenses)) },
        summary = { Text(stringResource(R.string.settings_licenses_desc)) },
        onClick = { context.startActivity(Intent(context, LicensesActivity::class.java)) }
    )
    Preference(
        title = { Text(stringResource(R.string.settings_github)) },
        summary = { Text("https://github.com/teddybaerd/NowbarMeter") },
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            uriHandler.openUri("https://github.com/teddybaerd/NowbarMeter")
        }
    )
}

@Composable
fun ColorPreference(
    title: String,
    color: Color,
    enabled: Boolean = true,
    showAlpha: Boolean = true,
    onColorSelected: (Color) -> Unit
) {
    val view = LocalView.current
    var showDialog by remember { mutableStateOf(false) }

    val theme = LocalPreferenceTheme.current
    TwoTargetPreference(
        title = {
            Text(
                title,
                color = if (enabled) Color.Unspecified else theme.summaryColor.copy(alpha = 0.38f)
            )
        },
        summary = {
            Text(
                "#%08X".format(color.toArgb()),
                color = if (enabled) theme.summaryColor else theme.summaryColor.copy(alpha = 0.38f)
            )
        },
        secondTarget = {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (enabled) color else color.copy(alpha = 0.38f))
            )
        },
        enabled = enabled,
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            showDialog = true
        }
    )

    if (showDialog) {
        ColorPickerDialog(
            initialColor = color,
            showAlpha = showAlpha,
            onDismiss = { showDialog = false },
            onColorSelected = {
                onColorSelected(it)
                showDialog = false
            }
        )
    }
}

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    showAlpha: Boolean = true,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val view = LocalView.current
    val controller = rememberColorPickerController()
    var selectedColor by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.color_picker_title)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    controller = controller,
                    initialColor = initialColor,
                    onColorChanged = { colorEnvelope ->
                        selectedColor = colorEnvelope.color
                    }
                )

                if (showAlpha) {
                    AlphaSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp),
                        controller = controller,
                        initialColor = initialColor
                    )
                }

                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp),
                    controller = controller,
                    initialColor = initialColor
                )

                AlphaTile(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    controller = controller
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onColorSelected(selectedColor)
            }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onDismiss()
            }) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

private fun performToggleHaptic(view: View, isChecked: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        view.performHapticFeedback(
            if (isChecked) HapticFeedbackConstants.TOGGLE_ON else HapticFeedbackConstants.TOGGLE_OFF
        )
    } else {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
}
