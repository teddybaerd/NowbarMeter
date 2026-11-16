// Modified by teddybaerd for the Nowbar Meter continuation (2026): independent app identity, localization, privacy controls and release maintenance.
package de.teddybaerd.nowbarmeter.data.repository

import android.os.SystemClock
import android.util.Log
import de.teddybaerd.nowbarmeter.data.model.AppThemeMode
import de.teddybaerd.nowbarmeter.data.source.NetSpeedData
import de.teddybaerd.nowbarmeter.data.source.impl.SpeedDataSource
import de.teddybaerd.nowbarmeter.format.SpeedFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class NetworkRepository(
    private val dataSource: SpeedDataSource,
    private val dataStoreRepository: DataStoreRepository,
) : KoinComponent {
    private val _isOverlayEnabled = MutableStateFlow(false)
    val isOverlayEnabled: StateFlow<Boolean> = _isOverlayEnabled.asStateFlow()

    private val _isLiveUpdateEnabled = MutableStateFlow(true)
    val isLiveUpdateEnabled: StateFlow<Boolean> = _isLiveUpdateEnabled.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(true)
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

    private val _isShowOnLockscreenEnabled = MutableStateFlow(false)
    val isShowOnLockscreenEnabled: StateFlow<Boolean> = _isShowOnLockscreenEnabled.asStateFlow()

    private val _isOverlayLocked = MutableStateFlow(false)
    val isOverlayLocked: StateFlow<Boolean> = _isOverlayLocked.asStateFlow()

    private val _isOverlayShowOnStatusBar = MutableStateFlow(false)
    val isOverlayShowOnStatusBar: StateFlow<Boolean> = _isOverlayShowOnStatusBar.asStateFlow()

    private val _isOverlayPortraitOnly = MutableStateFlow(false)
    val isOverlayPortraitOnly: StateFlow<Boolean> = _isOverlayPortraitOnly.asStateFlow()

    private val _isOverlayHideInImmersiveMode = MutableStateFlow(false)
    val isOverlayHideInImmersiveMode: StateFlow<Boolean> = _isOverlayHideInImmersiveMode.asStateFlow()

    private val _overlayAutoHideThreshold = MutableStateFlow(0L)
    val overlayAutoHideThreshold: StateFlow<Long> = _overlayAutoHideThreshold.asStateFlow()

    private val _isOverlayHideBackground = MutableStateFlow(false)
    val isOverlayHideBackground: StateFlow<Boolean> = _isOverlayHideBackground.asStateFlow()

    private val _overlayX = MutableStateFlow(100)
    val overlayX: StateFlow<Int> = _overlayX.asStateFlow()

    private val _overlayY = MutableStateFlow(200)
    val overlayY: StateFlow<Int> = _overlayY.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _netSpeed = MutableStateFlow(NetSpeedData(0, 0))
    val netSpeed: StateFlow<NetSpeedData> = _netSpeed.asStateFlow()

    private val _speedSamples = MutableSharedFlow<NetSpeedData>(replay = 1, extraBufferCapacity = 1)
    val speedSamples: SharedFlow<NetSpeedData> = _speedSamples.asSharedFlow()

    private val _samplingInterval = MutableStateFlow(1500L)
    val samplingInterval: StateFlow<Long> = _samplingInterval.asStateFlow()

    private val _overlayBgColor = MutableStateFlow(0xCC000000.toInt())
    val overlayBgColor: StateFlow<Int> = _overlayBgColor.asStateFlow()

    private val _overlayTextColor = MutableStateFlow(0xFFFFFFFF.toInt())
    val overlayTextColor: StateFlow<Int> = _overlayTextColor.asStateFlow()

    private val _overlayCornerRadius = MutableStateFlow(8)
    val overlayCornerRadius: StateFlow<Int> = _overlayCornerRadius.asStateFlow()

    private val _overlayPadding = MutableStateFlow(8)
    val overlayPadding: StateFlow<Int> = _overlayPadding.asStateFlow()

    private val _overlayTextSize = MutableStateFlow(10f)
    val overlayTextSize: StateFlow<Float> = _overlayTextSize.asStateFlow()

    private val _overlayTextUp = MutableStateFlow("▲ ")
    val overlayTextUp: StateFlow<String> = _overlayTextUp.asStateFlow()

    private val _overlayTextDown = MutableStateFlow("▼ ")
    val overlayTextDown: StateFlow<String> = _overlayTextDown.asStateFlow()

    private val _overlayOrderUpFirst = MutableStateFlow(false)
    val overlayOrderUpFirst: StateFlow<Boolean> = _overlayOrderUpFirst.asStateFlow()

    private val _overlayDirection = MutableStateFlow(0)
    val overlayDirection: StateFlow<Int> = _overlayDirection.asStateFlow()

    private val _overlayDisplayMode = MutableStateFlow(0)
    val overlayDisplayMode: StateFlow<Int> = _overlayDisplayMode.asStateFlow()

    private val _overlayAlignment = MutableStateFlow(0)
    val overlayAlignment: StateFlow<Int> = _overlayAlignment.asStateFlow()

    private val _overlayMeterSpacing = MutableStateFlow(8)
    val overlayMeterSpacing: StateFlow<Int> = _overlayMeterSpacing.asStateFlow()

    private val _notificationTextUp = MutableStateFlow("▲ ")
    val notificationTextUp: StateFlow<String> = _notificationTextUp.asStateFlow()

    private val _notificationTextDown = MutableStateFlow("▼ ")
    val notificationTextDown: StateFlow<String> = _notificationTextDown.asStateFlow()

    private val _notificationOrderUpFirst = MutableStateFlow(false)
    val notificationOrderUpFirst: StateFlow<Boolean> = _notificationOrderUpFirst.asStateFlow()

    private val _notificationDisplayMode = MutableStateFlow(0)
    val notificationDisplayMode: StateFlow<Int> = _notificationDisplayMode.asStateFlow()

    private val _notificationIconMode = MutableStateFlow(0)
    val notificationIconMode: StateFlow<Int> = _notificationIconMode.asStateFlow()

    private val _nowbarShowArrows = MutableStateFlow(false)
    val nowbarShowArrows: StateFlow<Boolean> = _nowbarShowArrows.asStateFlow()

    private val _nowbarExpandedMode = MutableStateFlow(0)
    val nowbarExpandedMode: StateFlow<Int> = _nowbarExpandedMode.asStateFlow()

    private val _notificationTextSize = MutableStateFlow(0.60f)
    val notificationTextSize: StateFlow<Float> = _notificationTextSize.asStateFlow()

    private val _notificationUnitSize = MutableStateFlow(0.45f)
    val notificationUnitSize: StateFlow<Float> = _notificationUnitSize.asStateFlow()

    private val _notificationThreshold = MutableStateFlow(0L)
    val notificationThreshold: StateFlow<Long> = _notificationThreshold.asStateFlow()

    private val _notificationLowTrafficMode = MutableStateFlow(0)
    val notificationLowTrafficMode: StateFlow<Int> = _notificationLowTrafficMode.asStateFlow()

    private val _notificationUseCustomColor = MutableStateFlow(false)
    val notificationUseCustomColor: StateFlow<Boolean> = _notificationUseCustomColor.asStateFlow()

    private val _notificationColor = MutableStateFlow(0xFF888888.toInt())
    val notificationColor: StateFlow<Int> = _notificationColor.asStateFlow()

    private val _isHideFromRecents = MutableStateFlow(false)
    val isHideFromRecents: StateFlow<Boolean> = _isHideFromRecents.asStateFlow()

    private val _isOverlayUseDefaultColors = MutableStateFlow(false)
    val isOverlayUseDefaultColors: StateFlow<Boolean> = _isOverlayUseDefaultColors.asStateFlow()

    private val _isAutoStartServiceEnabled = MutableStateFlow(false)
    val isAutoStartServiceEnabled: StateFlow<Boolean> = _isAutoStartServiceEnabled.asStateFlow()

    private val _speedUnit = MutableStateFlow("0")
    val speedUnit: StateFlow<String> = _speedUnit.asStateFlow()

    private val _speedRateUnit = MutableStateFlow(0)
    val speedRateUnit: StateFlow<Int> = _speedRateUnit.asStateFlow()

    private val _minSpeedUnit = MutableStateFlow(0)
    val minSpeedUnit: StateFlow<Int> = _minSpeedUnit.asStateFlow()

    private val _isHideKiloDecimalsEnabled = MutableStateFlow(true)
    val isHideKiloDecimalsEnabled: StateFlow<Boolean> = _isHideKiloDecimalsEnabled.asStateFlow()

    private val _isOledThemeEnabled = MutableStateFlow(false)
    val isOledThemeEnabled: StateFlow<Boolean> = _isOledThemeEnabled.asStateFlow()

    private val _appThemeMode = MutableStateFlow(AppThemeMode.Dynamic.value)
    val appThemeMode: StateFlow<Int> = _appThemeMode.asStateFlow()

    private val _appThemeColor = MutableStateFlow(AppThemeMode.DEFAULT_THEME_COLOR)
    val appThemeColor: StateFlow<Int> = _appThemeColor.asStateFlow()

    private val _fontPreset = MutableStateFlow(0)
    val fontPreset: StateFlow<Int> = _fontPreset.asStateFlow()

    private val _customFontPath = MutableStateFlow("")
    val customFontPath: StateFlow<String> = _customFontPath.asStateFlow()

    private val _customFontName = MutableStateFlow("")
    val customFontName: StateFlow<String> = _customFontName.asStateFlow()

    private val _isCompactSpeedTextEnabled = MutableStateFlow(true)
    val isCompactSpeedTextEnabled: StateFlow<Boolean> = _isCompactSpeedTextEnabled.asStateFlow()

    private val _isBlankNotificationEnabled = MutableStateFlow(false)
    val isBlankNotificationEnabled: StateFlow<Boolean> = _isBlankNotificationEnabled.asStateFlow()

    private val _isNotificationTransparentIconEnabled = MutableStateFlow(false)
    val isNotificationTransparentIconEnabled: StateFlow<Boolean> =
        _isNotificationTransparentIconEnabled.asStateFlow()

    val automaticUpdateChecks = dataStoreRepository.automaticUpdateChecks

    suspend fun setAutomaticUpdateChecks(enabled: Boolean) = dataStoreRepository.setAutomaticUpdateChecks(enabled)

    val skippedUpdateVersion: Flow<String> = dataStoreRepository.skippedUpdateVersion

    private var monitoringJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private var lastTotalRxBytes = 0L
    private var lastTotalTxBytes = 0L
    private var lastTime = 0L

    init {
        runBlocking {
            dataStoreRepository.allPreferences.first().let { prefs ->
                _isLiveUpdateEnabled.value = prefs[DataStoreRepository.KEY_LIVE_UPDATE] ?: true
                _isNotificationEnabled.value =
                    prefs[DataStoreRepository.KEY_NOTIFICATION_ENABLED] ?: true
                _isShowOnLockscreenEnabled.value =
                    prefs[DataStoreRepository.KEY_SHOW_ON_LOCKSCREEN] ?: false
                _isOverlayLocked.value = prefs[DataStoreRepository.KEY_OVERLAY_LOCKED] ?: false
                _isOverlayShowOnStatusBar.value =
                    prefs[DataStoreRepository.KEY_OVERLAY_SHOW_ON_STATUS_BAR] ?: false
                _isOverlayPortraitOnly.value =
                    prefs[DataStoreRepository.KEY_OVERLAY_PORTRAIT_ONLY] ?: false
                _isOverlayHideInImmersiveMode.value =
                    prefs[DataStoreRepository.KEY_OVERLAY_HIDE_IN_IMMERSIVE_MODE] ?: false
                _overlayAutoHideThreshold.value =
                    prefs[DataStoreRepository.KEY_OVERLAY_AUTO_HIDE_THRESHOLD] ?: 0L
                _isOverlayHideBackground.value =
                    prefs[DataStoreRepository.KEY_OVERLAY_HIDE_BACKGROUND] ?: false
                _overlayX.value = prefs[DataStoreRepository.KEY_OVERLAY_X] ?: 100
                _overlayY.value = prefs[DataStoreRepository.KEY_OVERLAY_Y] ?: 200
                _isOverlayEnabled.value = prefs[DataStoreRepository.KEY_OVERLAY_ENABLED] ?: false
                _samplingInterval.value =
                    prefs[DataStoreRepository.KEY_SAMPLING_INTERVAL] ?: 1500L
                _overlayBgColor.value =
                    prefs[DataStoreRepository.KEY_OVERLAY_BG_COLOR] ?: 0xCC000000.toInt()
                _overlayTextColor.value =
                    prefs[DataStoreRepository.KEY_OVERLAY_TEXT_COLOR] ?: 0xFFFFFFFF.toInt()
                _overlayCornerRadius.value =
                    prefs[DataStoreRepository.KEY_OVERLAY_CORNER_RADIUS] ?: 8
                _overlayPadding.value = prefs[DataStoreRepository.KEY_OVERLAY_PADDING] ?: 8
                _overlayTextSize.value = prefs[DataStoreRepository.KEY_OVERLAY_TEXT_SIZE] ?: 10f
                _overlayTextUp.value = prefs[DataStoreRepository.KEY_OVERLAY_TEXT_UP] ?: "▲ "
                _overlayTextDown.value = prefs[DataStoreRepository.KEY_OVERLAY_TEXT_DOWN] ?: "▼ "
                _overlayOrderUpFirst.value =
                    prefs[DataStoreRepository.KEY_OVERLAY_ORDER_UP_FIRST] ?: false
                _overlayDirection.value = prefs[DataStoreRepository.KEY_OVERLAY_DIRECTION] ?: 0
                _overlayDisplayMode.value =
                    (prefs[DataStoreRepository.KEY_OVERLAY_DISPLAY_MODE] ?: 0).let {
                        if (it in 0..3) it else 0
                    }
                _overlayAlignment.value = prefs[DataStoreRepository.KEY_OVERLAY_ALIGNMENT] ?: 0
                _overlayMeterSpacing.value =
                    prefs[DataStoreRepository.KEY_OVERLAY_METER_SPACING] ?: 8
                _notificationTextUp.value =
                    prefs[DataStoreRepository.KEY_NOTIFICATION_TEXT_UP] ?: "▲ "
                _notificationTextDown.value =
                    prefs[DataStoreRepository.KEY_NOTIFICATION_TEXT_DOWN] ?: "▼ "
                _notificationOrderUpFirst.value =
                    prefs[DataStoreRepository.KEY_NOTIFICATION_ORDER_UP_FIRST] ?: false
                _notificationDisplayMode.value =
                    prefs[DataStoreRepository.KEY_NOTIFICATION_DISPLAY_MODE] ?: 0
                _notificationIconMode.value =
                    (prefs[DataStoreRepository.KEY_NOTIFICATION_ICON_MODE] ?: 0).let {
                        if (it in 0..3) it else 0
                    }
                _nowbarShowArrows.value =
                    prefs[DataStoreRepository.KEY_NOWBAR_SHOW_ARROWS] ?: false
                _nowbarExpandedMode.value =
                    (prefs[DataStoreRepository.KEY_NOWBAR_EXPANDED_MODE] ?: 0).let {
                        if (it in 0..2) it else 0
                    }
                _notificationTextSize.value =
                    prefs[DataStoreRepository.KEY_NOTIFICATION_TEXT_SIZE] ?: 0.60f
                _notificationUnitSize.value =
                    prefs[DataStoreRepository.KEY_NOTIFICATION_UNIT_SIZE] ?: 0.45f
                _isHideFromRecents.value =
                    prefs[DataStoreRepository.KEY_HIDE_FROM_RECENTS] ?: false
                _isOverlayUseDefaultColors.value =
                    prefs[DataStoreRepository.KEY_OVERLAY_USE_DEFAULT_COLORS] ?: false
                _isAutoStartServiceEnabled.value =
                    prefs[DataStoreRepository.KEY_AUTO_START_SERVICE] ?: false
                _notificationThreshold.value =
                    prefs[DataStoreRepository.KEY_NOTIFICATION_THRESHOLD] ?: 0L
                _notificationLowTrafficMode.value =
                    prefs[DataStoreRepository.KEY_NOTIFICATION_LOW_TRAFFIC_MODE] ?: 0
                _notificationUseCustomColor.value =
                    prefs[DataStoreRepository.KEY_NOTIFICATION_USE_CUSTOM_COLOR] ?: false
                _notificationColor.value =
                    prefs[DataStoreRepository.KEY_NOTIFICATION_COLOR] ?: 0xFF888888.toInt()
                _speedUnit.value = prefs[DataStoreRepository.KEY_SPEED_UNIT] ?: "0"
                _speedRateUnit.value =
                    if (prefs[DataStoreRepository.KEY_SPEED_RATE_UNIT] == 1) 1 else 0
                _minSpeedUnit.value =
                    (prefs[DataStoreRepository.KEY_MIN_SPEED_UNIT] ?: 0).let {
                        if (it in 0..3) it else 0
                    }
                _isHideKiloDecimalsEnabled.value =
                    prefs[DataStoreRepository.KEY_HIDE_KILO_DECIMALS] ?: true
                _isOledThemeEnabled.value = prefs[DataStoreRepository.KEY_OLED_THEME] ?: false
                _appThemeMode.value =
                    AppThemeMode.fromValue(
                        prefs[DataStoreRepository.KEY_APP_THEME_MODE] ?: AppThemeMode.Dynamic.value
                    ).value
                _appThemeColor.value =
                    prefs[DataStoreRepository.KEY_APP_THEME_COLOR]
                        ?: AppThemeMode.DEFAULT_THEME_COLOR
                _fontPreset.value =
                    (prefs[DataStoreRepository.KEY_FONT_PRESET] ?: 0).let {
                        if (it in 0..4) it else 0
                    }
                _customFontPath.value = prefs[DataStoreRepository.KEY_CUSTOM_FONT_PATH] ?: ""
                _customFontName.value = prefs[DataStoreRepository.KEY_CUSTOM_FONT_NAME] ?: ""
                _isCompactSpeedTextEnabled.value =
                    prefs[DataStoreRepository.KEY_COMPACT_SPEED_TEXT] ?: true
                _isBlankNotificationEnabled.value =
                    prefs[DataStoreRepository.KEY_BLANK_NOTIFICATION] ?: false
                _isNotificationTransparentIconEnabled.value =
                    prefs[DataStoreRepository.KEY_NOTIFICATION_TRANSPARENT_ICON] ?: false
            }
        }
        scope.launch { dataStoreRepository.isLiveUpdateEnabled.collect { _isLiveUpdateEnabled.value = it } }
        scope.launch { dataStoreRepository.isNotificationEnabled.collect { _isNotificationEnabled.value = it } }
        scope.launch { dataStoreRepository.isShowOnLockscreenEnabled.collect { _isShowOnLockscreenEnabled.value = it } }
        scope.launch { dataStoreRepository.isOverlayLocked.collect { _isOverlayLocked.value = it } }
        scope.launch { dataStoreRepository.isOverlayShowOnStatusBar.collect { _isOverlayShowOnStatusBar.value = it } }
        scope.launch { dataStoreRepository.isOverlayPortraitOnly.collect { _isOverlayPortraitOnly.value = it } }
        scope.launch { dataStoreRepository.isOverlayHideInImmersiveMode.collect { _isOverlayHideInImmersiveMode.value = it } }
        scope.launch { dataStoreRepository.overlayAutoHideThreshold.collect { _overlayAutoHideThreshold.value = it } }
        scope.launch { dataStoreRepository.isOverlayHideBackground.collect { _isOverlayHideBackground.value = it } }
        scope.launch { dataStoreRepository.overlayX.collect { _overlayX.value = it } }
        scope.launch { dataStoreRepository.overlayY.collect { _overlayY.value = it } }
        scope.launch { dataStoreRepository.isOverlayEnabled.collect { _isOverlayEnabled.value = it } }
        scope.launch { dataStoreRepository.samplingInterval.collect { _samplingInterval.value = it } }
        scope.launch { dataStoreRepository.overlayBgColor.collect { _overlayBgColor.value = it } }
        scope.launch { dataStoreRepository.overlayTextColor.collect { _overlayTextColor.value = it } }
        scope.launch { dataStoreRepository.overlayCornerRadius.collect { _overlayCornerRadius.value = it } }
        scope.launch { dataStoreRepository.overlayPadding.collect { _overlayPadding.value = it } }
        scope.launch { dataStoreRepository.overlayTextSize.collect { _overlayTextSize.value = it } }
        scope.launch { dataStoreRepository.overlayTextUp.collect { _overlayTextUp.value = it } }
        scope.launch { dataStoreRepository.overlayTextDown.collect { _overlayTextDown.value = it } }
        scope.launch { dataStoreRepository.overlayOrderUpFirst.collect { _overlayOrderUpFirst.value = it } }
        scope.launch { dataStoreRepository.overlayDirection.collect { _overlayDirection.value = it } }
        scope.launch { dataStoreRepository.overlayDisplayMode.collect { _overlayDisplayMode.value = it } }
        scope.launch { dataStoreRepository.overlayAlignment.collect { _overlayAlignment.value = it } }
        scope.launch { dataStoreRepository.overlayMeterSpacing.collect { _overlayMeterSpacing.value = it } }
        scope.launch { dataStoreRepository.notificationTextUp.collect { _notificationTextUp.value = it } }
        scope.launch { dataStoreRepository.notificationTextDown.collect { _notificationTextDown.value = it } }
        scope.launch { dataStoreRepository.notificationOrderUpFirst.collect { _notificationOrderUpFirst.value = it } }
        scope.launch { dataStoreRepository.notificationDisplayMode.collect { _notificationDisplayMode.value = it } }
        scope.launch { dataStoreRepository.notificationIconMode.collect { _notificationIconMode.value = it } }
        scope.launch { dataStoreRepository.nowbarShowArrows.collect { _nowbarShowArrows.value = it } }
        scope.launch { dataStoreRepository.nowbarExpandedMode.collect { _nowbarExpandedMode.value = it } }
        scope.launch { dataStoreRepository.notificationTextSize.collect { _notificationTextSize.value = it } }
        scope.launch { dataStoreRepository.notificationUnitSize.collect { _notificationUnitSize.value = it } }
        scope.launch { dataStoreRepository.isHideFromRecents.collect { _isHideFromRecents.value = it } }
        scope.launch { dataStoreRepository.isOverlayUseDefaultColors.collect { _isOverlayUseDefaultColors.value = it } }
        scope.launch { dataStoreRepository.isAutoStartServiceEnabled.collect { _isAutoStartServiceEnabled.value = it } }
        scope.launch { dataStoreRepository.notificationThreshold.collect { _notificationThreshold.value = it } }
        scope.launch { dataStoreRepository.notificationLowTrafficMode.collect { _notificationLowTrafficMode.value = it } }
        scope.launch { dataStoreRepository.notificationUseCustomColor.collect { _notificationUseCustomColor.value = it } }
        scope.launch { dataStoreRepository.notificationColor.collect { _notificationColor.value = it } }
        scope.launch { dataStoreRepository.speedUnit.collect { _speedUnit.value = it } }
        scope.launch { dataStoreRepository.speedRateUnit.collect { _speedRateUnit.value = it } }
        scope.launch { dataStoreRepository.minSpeedUnit.collect { _minSpeedUnit.value = it } }
        scope.launch { dataStoreRepository.isHideKiloDecimalsEnabled.collect { _isHideKiloDecimalsEnabled.value = it } }
        scope.launch { dataStoreRepository.isOledThemeEnabled.collect { _isOledThemeEnabled.value = it } }
        scope.launch { dataStoreRepository.appThemeMode.collect { _appThemeMode.value = it } }
        scope.launch { dataStoreRepository.appThemeColor.collect { _appThemeColor.value = it } }
        scope.launch { dataStoreRepository.fontPreset.collect { _fontPreset.value = it } }
        scope.launch { dataStoreRepository.customFontPath.collect { _customFontPath.value = it } }
        scope.launch { dataStoreRepository.customFontName.collect { _customFontName.value = it } }
        scope.launch { dataStoreRepository.isCompactSpeedTextEnabled.collect { _isCompactSpeedTextEnabled.value = it } }
        scope.launch { dataStoreRepository.isBlankNotificationEnabled.collect { _isBlankNotificationEnabled.value = it } }
        scope.launch { dataStoreRepository.isNotificationTransparentIconEnabled.collect { _isNotificationTransparentIconEnabled.value = it } }
    }

    fun setOverlayEnabled(enable: Boolean) = scope.launch { dataStoreRepository.setOverlayEnabled(enable) }
    fun setLiveUpdateEnabled(enable: Boolean) = scope.launch { dataStoreRepository.setLiveUpdateEnabled(enable) }
    fun setNotificationEnabled(enable: Boolean) = scope.launch { dataStoreRepository.setNotificationEnabled(enable) }
    fun setShowOnLockscreenEnabled(enable: Boolean) = scope.launch { dataStoreRepository.setShowOnLockscreenEnabled(enable) }
    fun setOverlayLocked(locked: Boolean) = scope.launch { dataStoreRepository.setOverlayLocked(locked) }
    fun setOverlayShowOnStatusBar(enable: Boolean) = scope.launch { dataStoreRepository.setOverlayShowOnStatusBar(enable) }
    fun setOverlayPortraitOnly(enable: Boolean) = scope.launch { dataStoreRepository.setOverlayPortraitOnly(enable) }
    fun setOverlayHideInImmersiveMode(enable: Boolean) = scope.launch { dataStoreRepository.setOverlayHideInImmersiveMode(enable) }
    fun setOverlayAutoHideThreshold(threshold: Long) = scope.launch { dataStoreRepository.setOverlayAutoHideThreshold(threshold) }
    fun setOverlayHideBackground(hide: Boolean) = scope.launch { dataStoreRepository.setOverlayHideBackground(hide) }
    fun setOverlayX(x: Int) = scope.launch { dataStoreRepository.setOverlayX(x) }
    fun setOverlayY(y: Int) = scope.launch { dataStoreRepository.setOverlayY(y) }
    fun setSamplingInterval(interval: Long) = scope.launch { dataStoreRepository.setSamplingInterval(interval) }
    fun setOverlayBgColor(color: Int) = scope.launch { dataStoreRepository.setOverlayBgColor(color) }
    fun setOverlayTextColor(color: Int) = scope.launch { dataStoreRepository.setOverlayTextColor(color) }
    fun setOverlayCornerRadius(radius: Int) = scope.launch { dataStoreRepository.setOverlayCornerRadius(radius) }
    fun setOverlayPadding(padding: Int) = scope.launch { dataStoreRepository.setOverlayPadding(padding) }
    fun setOverlayTextSize(size: Float) = scope.launch { dataStoreRepository.setOverlayTextSize(size) }
    fun setOverlayTextUp(text: String) = scope.launch { dataStoreRepository.setOverlayTextUp(text) }
    fun setOverlayTextDown(text: String) = scope.launch { dataStoreRepository.setOverlayTextDown(text) }
    fun setOverlayOrderUpFirst(upFirst: Boolean) = scope.launch { dataStoreRepository.setOverlayOrderUpFirst(upFirst) }
    fun setOverlayDirection(direction: Int) = scope.launch { dataStoreRepository.setOverlayDirection(direction) }
    fun setOverlayDisplayMode(mode: Int) = scope.launch { dataStoreRepository.setOverlayDisplayMode(mode) }
    fun setOverlayAlignment(alignment: Int) = scope.launch { dataStoreRepository.setOverlayAlignment(alignment) }
    fun setOverlayMeterSpacing(spacing: Int) = scope.launch { dataStoreRepository.setOverlayMeterSpacing(spacing) }

    fun setNotificationTextUp(text: String) = scope.launch { dataStoreRepository.setNotificationTextUp(text) }
    fun setNotificationTextDown(text: String) = scope.launch { dataStoreRepository.setNotificationTextDown(text) }
    fun setNotificationOrderUpFirst(upFirst: Boolean) = scope.launch { dataStoreRepository.setNotificationOrderUpFirst(upFirst) }
    fun setNotificationDisplayMode(mode: Int) = scope.launch { dataStoreRepository.setNotificationDisplayMode(mode) }
    fun setNotificationIconMode(mode: Int) = scope.launch { dataStoreRepository.setNotificationIconMode(mode) }
    fun setNowbarShowArrows(show: Boolean) = scope.launch { dataStoreRepository.setNowbarShowArrows(show) }
    fun setNowbarExpandedMode(mode: Int) = scope.launch { dataStoreRepository.setNowbarExpandedMode(mode) }
    fun setNotificationTextSize(size: Float) = scope.launch { dataStoreRepository.setNotificationTextSize(size) }
    fun setNotificationUnitSize(size: Float) = scope.launch { dataStoreRepository.setNotificationUnitSize(size) }
    fun setHideFromRecents(hide: Boolean) = scope.launch { dataStoreRepository.setHideFromRecents(hide) }
    fun setOverlayUseDefaultColors(useDefault: Boolean) = scope.launch { dataStoreRepository.setOverlayUseDefaultColors(useDefault) }
    fun setAutoStartServiceEnabled(enabled: Boolean) = scope.launch { dataStoreRepository.setAutoStartServiceEnabled(enabled) }
    fun setNotificationThreshold(threshold: Long) = scope.launch { dataStoreRepository.setNotificationThreshold(threshold) }
    fun setNotificationLowTrafficMode(mode: Int) = scope.launch { dataStoreRepository.setNotificationLowTrafficMode(mode) }
    fun setNotificationUseCustomColor(useCustom: Boolean) = scope.launch { dataStoreRepository.setNotificationUseCustomColor(useCustom) }
    fun setNotificationColor(color: Int) = scope.launch { dataStoreRepository.setNotificationColor(color) }

    fun setSpeedUnit(unit: String) = scope.launch { dataStoreRepository.setSpeedUnit(unit) }
    fun setSpeedRateUnit(rateUnit: Int) = scope.launch { dataStoreRepository.setSpeedRateUnit(rateUnit) }
    fun setMinSpeedUnit(unit: Int) = scope.launch { dataStoreRepository.setMinSpeedUnit(unit) }
    fun setHideKiloDecimalsEnabled(enabled: Boolean) = scope.launch { dataStoreRepository.setHideKiloDecimalsEnabled(enabled) }

    fun setOledThemeEnabled(enabled: Boolean) = scope.launch { dataStoreRepository.setOledThemeEnabled(enabled) }
    fun setAppThemeMode(mode: Int) = scope.launch { dataStoreRepository.setAppThemeMode(mode) }
    fun setAppThemeColor(color: Int) = scope.launch { dataStoreRepository.setAppThemeColor(color) }

    fun setFontPreset(preset: Int) = scope.launch { dataStoreRepository.setFontPreset(preset) }
    fun setCustomFont(path: String, name: String) = scope.launch { dataStoreRepository.setCustomFont(path, name) }

    fun setCompactSpeedTextEnabled(enabled: Boolean) = scope.launch { dataStoreRepository.setCompactSpeedTextEnabled(enabled) }
    fun setBlankNotificationEnabled(enabled: Boolean) = scope.launch { dataStoreRepository.setBlankNotificationEnabled(enabled) }
    fun setNotificationTransparentIconEnabled(enabled: Boolean) = scope.launch { dataStoreRepository.setNotificationTransparentIconEnabled(enabled) }

    suspend fun setSkippedUpdateVersion(version: String) {
        dataStoreRepository.setSkippedUpdateVersion(version)
    }

    suspend fun getOverlayPosition(): Pair<Int, Int> {
        val x = dataStoreRepository.overlayX.first()
        val y = dataStoreRepository.overlayY.first()
        return x to y
    }

    fun saveOverlayPosition(x: Int, y: Int) {
        scope.launch {
            dataStoreRepository.saveOverlayPosition(x, y)
        }
    }

    fun startMonitoring() {
        if (_isMonitoring.value) return
        Log.i(TAG, "request start monitoring")
        _isMonitoring.value = true

        monitoringJob = scope.launch {
            val initialData = dataSource.getTrafficData()
            lastTotalRxBytes = initialData.rxBytes
            lastTotalTxBytes = initialData.txBytes
            lastTime = SystemClock.elapsedRealtime()

            while (isActive) {
                val startTime = SystemClock.elapsedRealtime()
                val interval = _samplingInterval.value

                withContext(Dispatchers.IO) {
                    val trafficData = dataSource.getTrafficData()
                    val totalRxBytes = trafficData.rxBytes
                    val totalTxBytes = trafficData.txBytes
                    val currentTime = SystemClock.elapsedRealtime()

                    val rxDelta = totalRxBytes - lastTotalRxBytes
                    val txDelta = totalTxBytes - lastTotalTxBytes
                    val timeDelta = currentTime - lastTime

                    val sample = if (timeDelta > 0) {
                        // When an interface disconnects or counters reset, delta is negative;
                        // report 0 rather than spiking on reconnect.
                        val downloadSpeed = if (rxDelta >= 0 && lastTotalRxBytes > 0L) {
                            (rxDelta * 1000L) / timeDelta
                        } else {
                            0L
                        }
                        val uploadSpeed = if (txDelta >= 0 && lastTotalTxBytes > 0L) {
                            (txDelta * 1000L) / timeDelta
                        } else {
                            0L
                        }
                        NetSpeedData(
                            downloadSpeed.coerceAtLeast(0L),
                            uploadSpeed.coerceAtLeast(0L)
                        )
                    } else {
                        _netSpeed.value
                    }

                    _netSpeed.value = sample
                    _speedSamples.tryEmit(sample)

                    lastTotalRxBytes = totalRxBytes
                    lastTotalTxBytes = totalTxBytes
                    lastTime = currentTime
                }

                val delayMills = interval - (SystemClock.elapsedRealtime() - startTime)
                delay(delayMills.coerceAtLeast(0L))
            }
        }
    }

    fun stopMonitoring() {
        Log.i(TAG, "request stop monitoring")
        monitoringJob?.cancel()
        monitoringJob = null
        _isMonitoring.value = false
        val zero = NetSpeedData(0, 0)
        _netSpeed.value = zero
        _speedSamples.tryEmit(zero)
    }

    companion object {
        private const val TAG = "NetworkRepository"

        fun formatSpeedText(
            bytes: Long,
            speedUnit: String = "0",
            compact: Boolean = false,
            hideKiloDecimals: Boolean = true,
            minSpeedUnit: Int = 0,
            rateUnit: Int = 0
        ): Pair<String, String> = SpeedFormatter.formatSpeedText(
            bytes = bytes,
            speedUnit = speedUnit,
            compact = compact,
            hideKiloDecimals = hideKiloDecimals,
            minSpeedUnit = minSpeedUnit,
            rateUnit = rateUnit
        )

        fun formatSpeedLine(
            bytes: Long,
            speedUnit: String = "0",
            compact: Boolean = false,
            hideKiloDecimals: Boolean = true,
            minSpeedUnit: Int = 0,
            rateUnit: Int = 0
        ): String = SpeedFormatter.formatSpeedLine(
            bytes = bytes,
            speedUnit = speedUnit,
            compact = compact,
            hideKiloDecimals = hideKiloDecimals,
            minSpeedUnit = minSpeedUnit,
            rateUnit = rateUnit
        )

        fun formatSpeedTextForLiveUpdate(
            bytes: Long,
            speedUnit: String = "0",
            compact: Boolean = false,
            hideKiloDecimals: Boolean = true,
            minSpeedUnit: Int = 0,
            rateUnit: Int = 0
        ): String = SpeedFormatter.formatSpeedTextForLiveUpdate(
            bytes = bytes,
            speedUnit = speedUnit,
            compact = compact,
            hideKiloDecimals = hideKiloDecimals,
            minSpeedUnit = minSpeedUnit,
            rateUnit = rateUnit
        )
    }
}
