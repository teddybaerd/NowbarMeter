// Modified by teddybaerd for the Nowbar Meter continuation (2026): independent app identity, localization, privacy controls and release maintenance.
package de.teddybaerd.nowbarmeter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.teddybaerd.nowbarmeter.data.model.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val DATA_STORE_NAME = "pixel_pulse_preferences"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

class DataStoreRepository(private val dataStore: DataStore<Preferences>) {

    /** Exposes raw Preferences Flow for batch initialization */
    val allPreferences: Flow<Preferences> = dataStore.data

    companion object {
        val KEY_LIVE_UPDATE = booleanPreferencesKey("key_live_update")
        val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("key_notification_enabled")
        val KEY_SHOW_ON_LOCKSCREEN = booleanPreferencesKey("key_show_on_lockscreen")
        val KEY_OVERLAY_ENABLED = booleanPreferencesKey("key_overlay_enabled")
        val KEY_OVERLAY_LOCKED = booleanPreferencesKey("key_overlay_locked")
        val KEY_OVERLAY_SHOW_ON_STATUS_BAR = booleanPreferencesKey("key_overlay_show_on_status_bar")
        val KEY_OVERLAY_PORTRAIT_ONLY = booleanPreferencesKey("key_overlay_portrait_only")
        val KEY_OVERLAY_HIDE_IN_IMMERSIVE_MODE =
            booleanPreferencesKey("key_overlay_hide_in_immersive_mode")
        val KEY_OVERLAY_AUTO_HIDE_THRESHOLD =
            longPreferencesKey("key_overlay_auto_hide_threshold")
        val KEY_OVERLAY_HIDE_BACKGROUND = booleanPreferencesKey("key_overlay_hide_background")
        val KEY_OVERLAY_X = intPreferencesKey("key_overlay_x")
        val KEY_OVERLAY_Y = intPreferencesKey("key_overlay_y")

        val KEY_SAMPLING_INTERVAL = longPreferencesKey("key_sampling_interval")
        val KEY_OVERLAY_BG_COLOR = intPreferencesKey("key_overlay_bg_color")
        val KEY_OVERLAY_TEXT_COLOR = intPreferencesKey("key_overlay_text_color")
        val KEY_OVERLAY_CORNER_RADIUS = intPreferencesKey("key_overlay_corner_radius")
        val KEY_OVERLAY_PADDING = intPreferencesKey("key_overlay_padding")
        val KEY_OVERLAY_TEXT_SIZE = floatPreferencesKey("key_overlay_text_size")
        val KEY_OVERLAY_TEXT_UP = stringPreferencesKey("key_overlay_text_up")
        val KEY_OVERLAY_TEXT_DOWN = stringPreferencesKey("key_overlay_text_down")
        val KEY_OVERLAY_ORDER_UP_FIRST = booleanPreferencesKey("key_overlay_order_up_first")
        val KEY_OVERLAY_DIRECTION = intPreferencesKey("key_overlay_direction")
        val KEY_OVERLAY_DISPLAY_MODE = intPreferencesKey("key_overlay_display_mode")
        val KEY_OVERLAY_ALIGNMENT = intPreferencesKey("key_overlay_alignment")
        val KEY_OVERLAY_METER_SPACING = intPreferencesKey("key_overlay_meter_spacing")

        val KEY_NOTIFICATION_TEXT_UP = stringPreferencesKey("key_notification_text_up")
        val KEY_NOTIFICATION_TEXT_DOWN = stringPreferencesKey("key_notification_text_down")
        val KEY_NOTIFICATION_ORDER_UP_FIRST =
            booleanPreferencesKey("key_notification_order_up_first")
        val KEY_NOTIFICATION_DISPLAY_MODE = intPreferencesKey("key_notification_display_mode")
        val KEY_NOTIFICATION_ICON_MODE = intPreferencesKey("key_notification_icon_mode")
        val KEY_NOWBAR_SHOW_ARROWS = booleanPreferencesKey("key_nowbar_show_arrows")
        val KEY_NOWBAR_EXPANDED_MODE = intPreferencesKey("key_nowbar_expanded_mode")
        val KEY_NOTIFICATION_TEXT_SIZE = floatPreferencesKey("key_notification_text_size")
        val KEY_NOTIFICATION_UNIT_SIZE = floatPreferencesKey("key_notification_unit_size")

        val KEY_HIDE_FROM_RECENTS = booleanPreferencesKey("key_hide_from_recents")
        val KEY_OVERLAY_USE_DEFAULT_COLORS = booleanPreferencesKey("key_overlay_use_default_colors")
        val KEY_AUTO_START_SERVICE = booleanPreferencesKey("key_auto_start_service")
        val KEY_NOTIFICATION_THRESHOLD = longPreferencesKey("key_notification_threshold")
        val KEY_NOTIFICATION_LOW_TRAFFIC_MODE =
            intPreferencesKey("key_notification_low_traffic_mode")
        val KEY_NOTIFICATION_USE_CUSTOM_COLOR =
            booleanPreferencesKey("key_notification_use_custom_color")
        val KEY_NOTIFICATION_COLOR = intPreferencesKey("key_notification_color")

        val KEY_SPEED_UNIT = stringPreferencesKey("key_speed_unit")
        val KEY_SPEED_RATE_UNIT = intPreferencesKey("key_speed_rate_unit")
        val KEY_MIN_SPEED_UNIT = intPreferencesKey("key_min_speed_unit")
        val KEY_HIDE_KILO_DECIMALS = booleanPreferencesKey("key_hide_kilo_decimals")

        val KEY_OLED_THEME = booleanPreferencesKey("key_oled_theme")
        val KEY_APP_THEME_MODE = intPreferencesKey("key_app_theme_mode")
        val KEY_APP_THEME_COLOR = intPreferencesKey("key_app_theme_color")

        val KEY_FONT_PRESET = intPreferencesKey("key_font_preset")
        val KEY_CUSTOM_FONT_PATH = stringPreferencesKey("key_custom_font_path")
        val KEY_CUSTOM_FONT_NAME = stringPreferencesKey("key_custom_font_name")

        val KEY_COMPACT_SPEED_TEXT = booleanPreferencesKey("key_compact_speed_text")
        val KEY_BLANK_NOTIFICATION = booleanPreferencesKey("key_blank_notification")
        val KEY_NOTIFICATION_TRANSPARENT_ICON =
            booleanPreferencesKey("key_notification_transparent_icon")
        val KEY_AUTO_UPDATE_CHECK = booleanPreferencesKey("key_auto_update_check")
        val KEY_SKIPPED_UPDATE_VERSION = stringPreferencesKey("key_skipped_update_version")
    }

    val isLiveUpdateEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_LIVE_UPDATE] ?: true }
    val isNotificationEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_NOTIFICATION_ENABLED] ?: true }
    val isShowOnLockscreenEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_SHOW_ON_LOCKSCREEN] ?: false }

    val isOverlayEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_OVERLAY_ENABLED] ?: false }
    val isOverlayLocked: Flow<Boolean> = dataStore.data.map { it[KEY_OVERLAY_LOCKED] ?: false }
    val isOverlayShowOnStatusBar: Flow<Boolean> = dataStore.data.map { it[KEY_OVERLAY_SHOW_ON_STATUS_BAR] ?: false }
    val isOverlayPortraitOnly: Flow<Boolean> = dataStore.data.map { it[KEY_OVERLAY_PORTRAIT_ONLY] ?: false }
    val isOverlayHideInImmersiveMode: Flow<Boolean> = dataStore.data.map { it[KEY_OVERLAY_HIDE_IN_IMMERSIVE_MODE] ?: false }
    val overlayAutoHideThreshold: Flow<Long> = dataStore.data.map { it[KEY_OVERLAY_AUTO_HIDE_THRESHOLD] ?: 0L }
    val isOverlayHideBackground: Flow<Boolean> = dataStore.data.map { it[KEY_OVERLAY_HIDE_BACKGROUND] ?: false }
    val overlayX: Flow<Int> = dataStore.data.map { it[KEY_OVERLAY_X] ?: 100 }
    val overlayY: Flow<Int> = dataStore.data.map { it[KEY_OVERLAY_Y] ?: 200 }

    val samplingInterval: Flow<Long> = dataStore.data.map { it[KEY_SAMPLING_INTERVAL] ?: 1500L }
    val overlayBgColor: Flow<Int> = dataStore.data.map { it[KEY_OVERLAY_BG_COLOR] ?: 0xCC000000.toInt() }
    val overlayTextColor: Flow<Int> = dataStore.data.map { it[KEY_OVERLAY_TEXT_COLOR] ?: 0xFFFFFFFF.toInt() }
    val overlayCornerRadius: Flow<Int> = dataStore.data.map { it[KEY_OVERLAY_CORNER_RADIUS] ?: 8 }
    val overlayPadding: Flow<Int> = dataStore.data.map { it[KEY_OVERLAY_PADDING] ?: 8 }
    val overlayTextSize: Flow<Float> = dataStore.data.map { it[KEY_OVERLAY_TEXT_SIZE] ?: 10f }
    val overlayTextUp: Flow<String> = dataStore.data.map { it[KEY_OVERLAY_TEXT_UP] ?: "▲ " }
    val overlayTextDown: Flow<String> = dataStore.data.map { it[KEY_OVERLAY_TEXT_DOWN] ?: "▼ " }
    val overlayOrderUpFirst: Flow<Boolean> = dataStore.data.map { it[KEY_OVERLAY_ORDER_UP_FIRST] ?: false }
    val overlayDirection: Flow<Int> = dataStore.data.map { it[KEY_OVERLAY_DIRECTION] ?: 0 }
    val overlayDisplayMode: Flow<Int> = dataStore.data.map { (it[KEY_OVERLAY_DISPLAY_MODE] ?: 0).let { m -> if (m in 0..3) m else 0 } }
    val overlayAlignment: Flow<Int> = dataStore.data.map { it[KEY_OVERLAY_ALIGNMENT] ?: 0 }
    val overlayMeterSpacing: Flow<Int> = dataStore.data.map { it[KEY_OVERLAY_METER_SPACING] ?: 8 }

    val notificationTextUp: Flow<String> = dataStore.data.map { it[KEY_NOTIFICATION_TEXT_UP] ?: "▲ " }
    val notificationTextDown: Flow<String> = dataStore.data.map { it[KEY_NOTIFICATION_TEXT_DOWN] ?: "▼ " }
    val notificationOrderUpFirst: Flow<Boolean> = dataStore.data.map { it[KEY_NOTIFICATION_ORDER_UP_FIRST] ?: false }
    val notificationDisplayMode: Flow<Int> = dataStore.data.map { it[KEY_NOTIFICATION_DISPLAY_MODE] ?: 0 }
    val notificationIconMode: Flow<Int> = dataStore.data.map { (it[KEY_NOTIFICATION_ICON_MODE] ?: 0).let { m -> if (m in 0..3) m else 0 } }
    val nowbarShowArrows: Flow<Boolean> = dataStore.data.map { it[KEY_NOWBAR_SHOW_ARROWS] ?: false }
    val nowbarExpandedMode: Flow<Int> = dataStore.data.map { (it[KEY_NOWBAR_EXPANDED_MODE] ?: 0).let { m -> if (m in 0..2) m else 0 } }
    val notificationTextSize: Flow<Float> = dataStore.data.map { it[KEY_NOTIFICATION_TEXT_SIZE] ?: 0.60f }
    val notificationUnitSize: Flow<Float> = dataStore.data.map { it[KEY_NOTIFICATION_UNIT_SIZE] ?: 0.45f }
    val notificationThreshold: Flow<Long> = dataStore.data.map { it[KEY_NOTIFICATION_THRESHOLD] ?: 0L }
    val notificationLowTrafficMode: Flow<Int> = dataStore.data.map { it[KEY_NOTIFICATION_LOW_TRAFFIC_MODE] ?: 0 }
    val notificationUseCustomColor: Flow<Boolean> = dataStore.data.map { it[KEY_NOTIFICATION_USE_CUSTOM_COLOR] ?: false }
    val notificationColor: Flow<Int> = dataStore.data.map { it[KEY_NOTIFICATION_COLOR] ?: 0xFF888888.toInt() }

    val isHideFromRecents: Flow<Boolean> = dataStore.data.map { it[KEY_HIDE_FROM_RECENTS] ?: false }
    val isOverlayUseDefaultColors: Flow<Boolean> = dataStore.data.map { it[KEY_OVERLAY_USE_DEFAULT_COLORS] ?: false }
    val isAutoStartServiceEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_AUTO_START_SERVICE] ?: false }

    val speedUnit: Flow<String> = dataStore.data.map { it[KEY_SPEED_UNIT] ?: "0" }
    val speedRateUnit: Flow<Int> = dataStore.data.map { if (it[KEY_SPEED_RATE_UNIT] == 1) 1 else 0 }
    val minSpeedUnit: Flow<Int> = dataStore.data.map { (it[KEY_MIN_SPEED_UNIT] ?: 0).let { m -> if (m in 0..3) m else 0 } }
    val isHideKiloDecimalsEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_HIDE_KILO_DECIMALS] ?: true }

    val isOledThemeEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_OLED_THEME] ?: false }
    val appThemeMode: Flow<Int> = dataStore.data.map { AppThemeMode.fromValue(it[KEY_APP_THEME_MODE] ?: AppThemeMode.Dynamic.value).value }
    val appThemeColor: Flow<Int> = dataStore.data.map { it[KEY_APP_THEME_COLOR] ?: AppThemeMode.DEFAULT_THEME_COLOR }

    val fontPreset: Flow<Int> = dataStore.data.map { (it[KEY_FONT_PRESET] ?: 0).let { p -> if (p in 0..4) p else 0 } }
    val customFontPath: Flow<String> = dataStore.data.map { it[KEY_CUSTOM_FONT_PATH] ?: "" }
    val customFontName: Flow<String> = dataStore.data.map { it[KEY_CUSTOM_FONT_NAME] ?: "" }

    val isCompactSpeedTextEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_COMPACT_SPEED_TEXT] ?: true }
    val isBlankNotificationEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_BLANK_NOTIFICATION] ?: false }
    val isNotificationTransparentIconEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_NOTIFICATION_TRANSPARENT_ICON] ?: false }
    val automaticUpdateChecks: Flow<Boolean> = dataStore.data.map { it[KEY_AUTO_UPDATE_CHECK] ?: true }

    suspend fun setAutomaticUpdateChecks(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_UPDATE_CHECK] = enabled }
    }

    val skippedUpdateVersion: Flow<String> = dataStore.data.map { it[KEY_SKIPPED_UPDATE_VERSION] ?: "" }

    suspend fun setLiveUpdateEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_LIVE_UPDATE] = enabled
            if (enabled) {
                preferences[KEY_BLANK_NOTIFICATION] = false
            }
        }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun setShowOnLockscreenEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_SHOW_ON_LOCKSCREEN] = enabled }
    }

    suspend fun setOverlayEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_OVERLAY_ENABLED] = enabled }
    }

    suspend fun setOverlayLocked(locked: Boolean) {
        dataStore.edit { it[KEY_OVERLAY_LOCKED] = locked }
    }

    suspend fun setOverlayShowOnStatusBar(enabled: Boolean) {
        dataStore.edit { it[KEY_OVERLAY_SHOW_ON_STATUS_BAR] = enabled }
    }

    suspend fun setOverlayPortraitOnly(enabled: Boolean) {
        dataStore.edit { it[KEY_OVERLAY_PORTRAIT_ONLY] = enabled }
    }

    suspend fun setOverlayHideInImmersiveMode(enabled: Boolean) {
        dataStore.edit { it[KEY_OVERLAY_HIDE_IN_IMMERSIVE_MODE] = enabled }
    }

    suspend fun setOverlayAutoHideThreshold(threshold: Long) {
        dataStore.edit { it[KEY_OVERLAY_AUTO_HIDE_THRESHOLD] = threshold }
    }

    suspend fun setOverlayHideBackground(hide: Boolean) {
        dataStore.edit { it[KEY_OVERLAY_HIDE_BACKGROUND] = hide }
    }

    suspend fun saveOverlayPosition(x: Int, y: Int) {
        dataStore.edit {
            it[KEY_OVERLAY_X] = x
            it[KEY_OVERLAY_Y] = y
        }
    }

    suspend fun setOverlayX(x: Int) {
        dataStore.edit { it[KEY_OVERLAY_X] = x }
    }

    suspend fun setOverlayY(y: Int) {
        dataStore.edit { it[KEY_OVERLAY_Y] = y }
    }

    suspend fun setSamplingInterval(interval: Long) {
        dataStore.edit { it[KEY_SAMPLING_INTERVAL] = interval }
    }

    suspend fun setOverlayBgColor(color: Int) {
        dataStore.edit { it[KEY_OVERLAY_BG_COLOR] = color }
    }

    suspend fun setOverlayTextColor(color: Int) {
        dataStore.edit { it[KEY_OVERLAY_TEXT_COLOR] = color }
    }

    suspend fun setOverlayCornerRadius(radius: Int) {
        dataStore.edit { it[KEY_OVERLAY_CORNER_RADIUS] = radius }
    }

    suspend fun setOverlayPadding(padding: Int) {
        dataStore.edit { it[KEY_OVERLAY_PADDING] = padding }
    }

    suspend fun setOverlayTextSize(size: Float) {
        dataStore.edit { it[KEY_OVERLAY_TEXT_SIZE] = size }
    }

    suspend fun setOverlayTextUp(text: String) {
        dataStore.edit { it[KEY_OVERLAY_TEXT_UP] = text }
    }

    suspend fun setOverlayTextDown(text: String) {
        dataStore.edit { it[KEY_OVERLAY_TEXT_DOWN] = text }
    }

    suspend fun setOverlayOrderUpFirst(upFirst: Boolean) {
        dataStore.edit { it[KEY_OVERLAY_ORDER_UP_FIRST] = upFirst }
    }

    suspend fun setOverlayDirection(direction: Int) {
        dataStore.edit { it[KEY_OVERLAY_DIRECTION] = direction }
    }

    suspend fun setOverlayDisplayMode(mode: Int) {
        dataStore.edit { it[KEY_OVERLAY_DISPLAY_MODE] = if (mode in 0..3) mode else 0 }
    }

    suspend fun setOverlayAlignment(alignment: Int) {
        dataStore.edit { it[KEY_OVERLAY_ALIGNMENT] = alignment }
    }

    suspend fun setOverlayMeterSpacing(spacing: Int) {
        dataStore.edit { it[KEY_OVERLAY_METER_SPACING] = spacing }
    }

    suspend fun setNotificationTextUp(text: String) {
        dataStore.edit { it[KEY_NOTIFICATION_TEXT_UP] = text }
    }

    suspend fun setNotificationTextDown(text: String) {
        dataStore.edit { it[KEY_NOTIFICATION_TEXT_DOWN] = text }
    }

    suspend fun setNotificationOrderUpFirst(upFirst: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATION_ORDER_UP_FIRST] = upFirst }
    }

    suspend fun setNotificationDisplayMode(mode: Int) {
        dataStore.edit { it[KEY_NOTIFICATION_DISPLAY_MODE] = mode }
    }

    suspend fun setNotificationIconMode(mode: Int) {
        dataStore.edit { it[KEY_NOTIFICATION_ICON_MODE] = if (mode in 0..3) mode else 0 }
    }

    suspend fun setNowbarShowArrows(show: Boolean) {
        dataStore.edit { it[KEY_NOWBAR_SHOW_ARROWS] = show }
    }

    suspend fun setNowbarExpandedMode(mode: Int) {
        dataStore.edit { it[KEY_NOWBAR_EXPANDED_MODE] = if (mode in 0..2) mode else 0 }
    }

    suspend fun setNotificationTextSize(size: Float) {
        dataStore.edit { it[KEY_NOTIFICATION_TEXT_SIZE] = size }
    }

    suspend fun setNotificationUnitSize(size: Float) {
        dataStore.edit { it[KEY_NOTIFICATION_UNIT_SIZE] = size }
    }

    suspend fun setNotificationThreshold(threshold: Long) {
        dataStore.edit { it[KEY_NOTIFICATION_THRESHOLD] = threshold }
    }

    suspend fun setNotificationLowTrafficMode(mode: Int) {
        dataStore.edit { it[KEY_NOTIFICATION_LOW_TRAFFIC_MODE] = mode }
    }

    suspend fun setNotificationUseCustomColor(useCustom: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATION_USE_CUSTOM_COLOR] = useCustom }
    }

    suspend fun setNotificationColor(color: Int) {
        dataStore.edit { it[KEY_NOTIFICATION_COLOR] = color }
    }

    suspend fun setHideFromRecents(hide: Boolean) {
        dataStore.edit { it[KEY_HIDE_FROM_RECENTS] = hide }
    }

    suspend fun setOverlayUseDefaultColors(useDefault: Boolean) {
        dataStore.edit { it[KEY_OVERLAY_USE_DEFAULT_COLORS] = useDefault }
    }

    suspend fun setAutoStartServiceEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_START_SERVICE] = enabled }
    }

    suspend fun setSpeedUnit(unit: String) {
        dataStore.edit { it[KEY_SPEED_UNIT] = unit }
    }

    suspend fun setSpeedRateUnit(rateUnit: Int) {
        dataStore.edit { it[KEY_SPEED_RATE_UNIT] = if (rateUnit == 1) 1 else 0 }
    }

    suspend fun setMinSpeedUnit(unit: Int) {
        dataStore.edit { it[KEY_MIN_SPEED_UNIT] = if (unit in 0..3) unit else 0 }
    }

    suspend fun setHideKiloDecimalsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_HIDE_KILO_DECIMALS] = enabled }
    }

    suspend fun setOledThemeEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_OLED_THEME] = enabled }
    }

    suspend fun setAppThemeMode(mode: Int) {
        dataStore.edit { it[KEY_APP_THEME_MODE] = AppThemeMode.fromValue(mode).value }
    }

    suspend fun setAppThemeColor(color: Int) {
        dataStore.edit { it[KEY_APP_THEME_COLOR] = color }
    }

    suspend fun setFontPreset(preset: Int) {
        dataStore.edit { it[KEY_FONT_PRESET] = if (preset in 0..4) preset else 0 }
    }

    suspend fun setCustomFont(path: String, name: String) {
        dataStore.edit {
            it[KEY_CUSTOM_FONT_PATH] = path
            it[KEY_CUSTOM_FONT_NAME] = name
            it[KEY_FONT_PRESET] = 4
        }
    }

    suspend fun setCompactSpeedTextEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_COMPACT_SPEED_TEXT] = enabled }
    }

    suspend fun setBlankNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_BLANK_NOTIFICATION] = enabled
            if (enabled) {
                preferences[KEY_LIVE_UPDATE] = false
            }
        }
    }

    suspend fun setNotificationTransparentIconEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATION_TRANSPARENT_ICON] = enabled }
    }

    suspend fun setSkippedUpdateVersion(version: String) {
        dataStore.edit { it[KEY_SKIPPED_UPDATE_VERSION] = version }
    }
}
