package de.teddybaerd.nowbarmeter.service

import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import de.teddybaerd.nowbarmeter.data.repository.NetworkRepository
import de.teddybaerd.nowbarmeter.data.source.NetSpeedData
import de.teddybaerd.nowbarmeter.ui.overlay.OverlayWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class NetworkMonitorService : Service() {
    companion object {
        private const val TAG = "NetworkMonitorService"
    }

    private val repository: NetworkRepository by inject()
    private val notificationHelper: NotificationHelper by inject()
    private val notificationManager: NotificationManager by inject()
    private val overlayWindow: OverlayWindow by inject()
    private val keyguardManager by lazy { getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager }

    private var serviceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateNotificationManually()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)

        val screenStateFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenStateReceiver, screenStateFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenStateReceiver, screenStateFilter)
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationHelper.resetRenderCache()
        val isLocked = keyguardManager.isKeyguardLocked
        val forceLiveUpdate = repository.isShowOnLockscreenEnabled.value && isLocked

        val initialNotif = notificationHelper.buildNotification(
            speed = repository.netSpeed.value,
            isLiveUpdate = repository.isLiveUpdateEnabled.value,
            isNotificationEnabled = repository.isNotificationEnabled.value,
            textUp = repository.notificationTextUp.value,
            textDown = repository.notificationTextDown.value,
            upFirst = repository.notificationOrderUpFirst.value,
            displayMode = repository.notificationDisplayMode.value,
            iconMode = repository.notificationIconMode.value,
            nowbarShowArrows = repository.nowbarShowArrows.value,
            nowbarExpandedMode = repository.nowbarExpandedMode.value,
            textSize = repository.notificationTextSize.value,
            unitSize = repository.notificationUnitSize.value,
            threshold = repository.notificationThreshold.value,
            lowTrafficMode = repository.notificationLowTrafficMode.value,
            useCustomColor = repository.notificationUseCustomColor.value,
            color = repository.notificationColor.value,
            speedUnit = repository.speedUnit.value,
            minSpeedUnit = repository.minSpeedUnit.value,
            rateUnit = repository.speedRateUnit.value,
            compactMode = repository.isCompactSpeedTextEnabled.value,
            hideKiloDecimals = repository.isHideKiloDecimalsEnabled.value,
            isBlank = repository.isBlankNotificationEnabled.value,
            isTransparentIcon = repository.isNotificationTransparentIconEnabled.value,
            forceLiveUpdateOnLockscreen = forceLiveUpdate,
            fontPreset = repository.fontPreset.value,
            customFontPath = repository.customFontPath.value
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NotificationHelper.NOTIFICATION_ID,
                    initialNotif,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(
                    NotificationHelper.NOTIFICATION_ID,
                    initialNotif,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "onStartCommand: start foreground error", e)
            stopSelf()
            return START_NOT_STICKY
        }

        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        serviceJob?.cancel()
        repository.startMonitoring()

        serviceJob = scope.launch {
            repository.speedSamples.collect { speed ->
                updateNotification(speed)
            }
        }
    }

    private suspend fun updateNotification(speed: NetSpeedData) {
        withContext(Dispatchers.Main) {
            try {
                if (repository.isOverlayEnabled.value) {
                    if (Settings.canDrawOverlays(this@NetworkMonitorService)) {
                        overlayWindow.show()
                        overlayWindow.update(speed)
                    } else {
                        Log.w(TAG, "overlay enabled but permission not granted, hiding overlay.")
                        overlayWindow.hide()
                    }
                } else {
                    overlayWindow.hide()
                }
            } catch (e: Exception) {
                Log.w(TAG, "startMonitoring: overlay window error", e)
            }
        }

        val notification = withContext(Dispatchers.Default) {
            val isLocked = keyguardManager.isKeyguardLocked
            val forceLiveUpdate = repository.isShowOnLockscreenEnabled.value && isLocked

            notificationHelper.buildIfChanged(
                speed = speed,
                isLiveUpdate = repository.isLiveUpdateEnabled.value,
                isNotificationEnabled = repository.isNotificationEnabled.value,
                textUp = repository.notificationTextUp.value,
                textDown = repository.notificationTextDown.value,
                upFirst = repository.notificationOrderUpFirst.value,
                displayMode = repository.notificationDisplayMode.value,
                iconMode = repository.notificationIconMode.value,
                nowbarShowArrows = repository.nowbarShowArrows.value,
                nowbarExpandedMode = repository.nowbarExpandedMode.value,
                textSize = repository.notificationTextSize.value,
                unitSize = repository.notificationUnitSize.value,
                threshold = repository.notificationThreshold.value,
                lowTrafficMode = repository.notificationLowTrafficMode.value,
                useCustomColor = repository.notificationUseCustomColor.value,
                color = repository.notificationColor.value,
                speedUnit = repository.speedUnit.value,
                minSpeedUnit = repository.minSpeedUnit.value,
                rateUnit = repository.speedRateUnit.value,
                compactMode = repository.isCompactSpeedTextEnabled.value,
                hideKiloDecimals = repository.isHideKiloDecimalsEnabled.value,
                isBlank = repository.isBlankNotificationEnabled.value,
                isTransparentIcon = repository.isNotificationTransparentIconEnabled.value,
                forceLiveUpdateOnLockscreen = forceLiveUpdate,
                fontPreset = repository.fontPreset.value,
                customFontPath = repository.customFontPath.value
            )
        }
        if (notification != null) {
            notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotificationManually() {
        scope.launch {
            notificationHelper.resetRenderCache()
            updateNotification(repository.netSpeed.value)
        }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "Screen OFF: Scheduling sleep in 2 minutes")
                    stopMonitoringJob?.cancel()
                    stopMonitoringJob = scope.launch {
                        delay(2 * 60 * 1000L)
                        Log.d(TAG, "Screen OFF Timeout: Stopping monitoring to save power")
                        repository.stopMonitoring()
                    }
                }

                Intent.ACTION_SCREEN_ON -> {
                    Log.d(TAG, "Screen ON: Cancelling sleep timer")
                    stopMonitoringJob?.cancel()
                    if (!repository.isMonitoring.value) {
                        Log.d(TAG, "Screen ON: Resuming monitoring")
                        startMonitoring()
                    }
                }
            }
        }
    }

    private var stopMonitoringJob: Job? = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
        unregisterReceiver(screenStateReceiver)
        serviceJob?.cancel()
        stopMonitoringJob?.cancel()
        overlayWindow.hide()
        repository.stopMonitoring()
        notificationHelper.resetRenderCache()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
