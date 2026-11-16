// Modified by teddybaerd for the Nowbar Meter continuation (2026): independent app identity, localization, privacy controls and release maintenance.
package de.teddybaerd.nowbarmeter.ui

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.teddybaerd.nowbarmeter.R
import de.teddybaerd.nowbarmeter.BuildConfig
import de.teddybaerd.nowbarmeter.data.repository.NetworkRepository
import de.teddybaerd.nowbarmeter.permissions.DisplayPermissionState
import de.teddybaerd.nowbarmeter.permissions.ServiceStartCheckResult
import de.teddybaerd.nowbarmeter.service.NetworkMonitorService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel(
    private val application: Application,
) : AndroidViewModel(application), KoinComponent {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private val repository: NetworkRepository by inject()

    val currentSpeed = repository.netSpeed

    val isOverlayEnabled = repository.isOverlayEnabled
    val isNotificationEnabled = repository.isNotificationEnabled
    val isHideFromRecents = repository.isHideFromRecents

    val speedUnit = repository.speedUnit
    val speedRateUnit = repository.speedRateUnit
    val minSpeedUnit = repository.minSpeedUnit
    val isHideKiloDecimalsEnabled = repository.isHideKiloDecimalsEnabled

    val isOledThemeEnabled = repository.isOledThemeEnabled
    val appThemeMode = repository.appThemeMode
    val appThemeColor = repository.appThemeColor

    val fontPreset = repository.fontPreset
    val customFontPath = repository.customFontPath

    val isCompactSpeedTextEnabled = repository.isCompactSpeedTextEnabled
    val isBlankNotificationEnabled = repository.isBlankNotificationEnabled

    val isServiceRunning = repository.isMonitoring

    private val _serviceStartError = MutableStateFlow<Pair<String, String>?>(null)
    val serviceStartError = _serviceStartError.asStateFlow()

    data class UpdateInfo(
        val versionName: String,
        val htmlUrl: String,
        val apkDownloadUrl: String?,
        val releaseNotes: String
    )

    private val _updateAvailable = MutableStateFlow<UpdateInfo?>(null)
    val updateAvailable = _updateAvailable.asStateFlow()

    val automaticUpdateChecks = repository.automaticUpdateChecks
    val skippedUpdateVersion = repository.skippedUpdateVersion
    private val automaticCheck = AutomaticUpdateCheck()
    private val _updateStatus = MutableStateFlow<Int?>(null)
    val updateStatus = _updateStatus.asStateFlow()

    fun setAutomaticUpdateChecks(enabled: Boolean) {
        viewModelScope.launch { repository.setAutomaticUpdateChecks(enabled) }
    }

    fun checkAutomatically() {
        viewModelScope.launch {
            // Wait for persisted preferences: no optimistic default can trigger a request.
            automaticCheck.run(enabled = { repository.automaticUpdateChecks.first() }) {
                checkForUpdates(BuildConfig.VERSION_NAME, repository.skippedUpdateVersion.first())
            }
        }
    }

    fun checkManually() = checkForUpdates(BuildConfig.VERSION_NAME, "", manual = true)

    private fun checkForUpdates(currentVersion: String, skippedVersion: String, manual: Boolean = false) {
        if (_updateStatus.value == R.string.update_checking) return
        _updateStatus.value = R.string.update_checking
        viewModelScope.launch(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("https://api.github.com/repos/teddybaerd/NowbarMeter/releases/latest")
                val activeConnection = url.openConnection() as HttpURLConnection
                connection = activeConnection
                activeConnection.requestMethod = "GET"
                activeConnection.connectTimeout = 5000
                activeConnection.readTimeout = 5000

                check(activeConnection.responseCode == HttpURLConnection.HTTP_OK) { "Update server returned HTTP ${activeConnection.responseCode}" }
                run {
                    val response = activeConnection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    val tagName = json.getString("tag_name")
                    val htmlUrl = json.getString("html_url")
                    val body = json.optString("body", "").trim()

                    var apkUrl: String? = null
                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.optJSONObject(i) ?: continue
                            val name = asset.optString("name", "")
                            val downloadUrl = asset.optString("browser_download_url", "")
                            if (name.endsWith("-${BuildConfig.FLAVOR}.apk", ignoreCase = true) && downloadUrl.isNotEmpty()) {
                                apkUrl = downloadUrl
                                break
                            }
                        }
                    }

                    val cleanedTag = tagName.removePrefix("v")

                    _updateStatus.value = if (isNewerVersion(cleanedTag, currentVersion)) R.string.update_found else R.string.update_current
                    if (isNewerVersion(cleanedTag, currentVersion) && cleanedTag != skippedVersion) {
                        _updateAvailable.value = UpdateInfo(
                            versionName = cleanedTag,
                            htmlUrl = htmlUrl,
                            apkDownloadUrl = apkUrl,
                            releaseNotes = body
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _updateStatus.value = if (manual) R.string.update_failed else null
                Log.e(TAG, "checkForUpdates: failed to check for updates", e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val maxLength = maxOf(latestParts.size, currentParts.size)

        for (i in 0 until maxLength) {
            val l = if (i < latestParts.size) latestParts[i] else 0
            val c = if (i < currentParts.size) currentParts[i] else 0
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    fun skipUpdate(version: String) {
        viewModelScope.launch {
            repository.setSkippedUpdateVersion(version)
            _updateAvailable.value = null
        }
    }

    fun clearUpdateDialog() {
        _updateAvailable.value = null
    }

    private fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    fun startService() {
        _serviceStartError.value = null

        val checkResult = DisplayPermissionState.evaluateServiceStart(
            isOverlayEnabled = isOverlayEnabled.value,
            isNotificationEnabled = isNotificationEnabled.value,
            hasOverlayPermission = Settings.canDrawOverlays(application),
            hasNotificationPermission = hasNotificationPermission()
        )

        when (checkResult) {
            ServiceStartCheckResult.MissingOverlayPermission -> {
                _serviceStartError.value =
                    application.getString(R.string.error_overlay_permission) to Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                return
            }

            ServiceStartCheckResult.MissingNotificationPermission -> {
                _serviceStartError.value =
                    application.getString(R.string.error_notification_permission) to Settings.ACTION_APP_NOTIFICATION_SETTINGS
                return
            }

            ServiceStartCheckResult.Ready -> Unit
        }

        val intent = Intent(application, NetworkMonitorService::class.java)
        try {
            application.startForegroundService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "startService: start foreground service error", e)
            _serviceStartError.value =
                application.getString(
                    R.string.error_service_start_failed,
                    e.message
                ) to Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        }
    }

    fun stopService(clearError: Boolean = true) {
        val intent = Intent(application, NetworkMonitorService::class.java)
        application.stopService(intent)

        if (clearError) {
            clearError()
        }
    }

    fun clearError() {
        _serviceStartError.value = null
    }

    fun setOverlayEnabled(enable: Boolean) {
        if (enable && isServiceRunning.value) {
            if (!Settings.canDrawOverlays(application)) {
                _serviceStartError.value =
                    application.getString(R.string.error_overlay_permission) to Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                if (!hasNotificationPermission()) {
                    stopService(false)
                }
                return
            }
        }
        repository.setOverlayEnabled(enable)
    }

    fun setNotificationEnabled(enable: Boolean) {
        if (enable && isServiceRunning.value) {
            if (!hasNotificationPermission()) {
                _serviceStartError.value =
                    application.getString(R.string.error_notification_permission) to Settings.ACTION_APP_NOTIFICATION_SETTINGS
                if (!isOverlayEnabled.value || !Settings.canDrawOverlays(application)) {
                    stopService(false)
                }
                return
            }
        }
        repository.setNotificationEnabled(enable)
    }
}
