// Modified by teddybaerd for the Nowbar Meter continuation (2026): independent app identity, localization, privacy controls and release maintenance.
package de.teddybaerd.nowbarmeter.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import de.teddybaerd.nowbarmeter.MainActivity
import de.teddybaerd.nowbarmeter.R
import de.teddybaerd.nowbarmeter.data.source.NetSpeedData
import de.teddybaerd.nowbarmeter.format.SpeedFormatter
import de.teddybaerd.nowbarmeter.ui.theme.CustomFontManager
import kotlin.math.roundToInt

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "net_monitor_silent"
        const val NOTIFICATION_ID = 1001

        fun createNotificationChannel(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val group = NotificationChannelGroup(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name)
            )
            notificationManager.createNotificationChannelGroup(group)

            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                setShowBadge(false)
                setGroup(CHANNEL_ID)
                setSound(null, null)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private data class NotificationRenderState(
        val mode: String,
        val contentText: String,
        val statusText: String = "",
        val expandedText: String = "",
        val valueText: String = "",
        val unitText: String = "",
        val textSize: Float = 0.60f,
        val unitSize: Float = 0.45f,
        val isBlank: Boolean = false,
        val isTransparentIcon: Boolean = false,
        val useCustomColor: Boolean = false,
        val color: Int = 0,
        val fontPreset: Int = 0,
        val customFontPath: String = ""
    )

    private var lastRenderState: NotificationRenderState? = null
    private var lastPostedAtMillis: Long = System.currentTimeMillis()

    private val size =
        (context.resources.displayMetrics.density * 24).roundToInt().coerceAtLeast(48)
    private val bitmap = createBitmap(size, size)
    private val canvas = Canvas(bitmap)

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textSize = size * 0.60f
    }

    private val unitPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textSize = size * 0.45f
    }

    private fun applySamsungHack(notification: Notification): Notification {
        try {
            val semFlagsField = Notification::class.java.getDeclaredField("semFlags")
            semFlagsField.isAccessible = true
            val currentFlags = semFlagsField.getInt(notification)
            semFlagsField.setInt(notification, currentFlags or 32768)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return notification
    }

    fun resetRenderCache() {
        lastRenderState = null
    }

    fun buildIfChanged(
        speed: NetSpeedData,
        isLiveUpdate: Boolean,
        isNotificationEnabled: Boolean,
        textUp: String,
        textDown: String,
        upFirst: Boolean,
        displayMode: Int,
        iconMode: Int = 0,
        nowbarShowArrows: Boolean = false,
        nowbarExpandedMode: Int = 0,
        textSize: Float = 0.60f,
        unitSize: Float = 0.45f,
        threshold: Long = 0L,
        lowTrafficMode: Int = 0,
        useCustomColor: Boolean = false,
        color: Int = 0,
        speedUnit: String = "0",
        minSpeedUnit: Int = 0,
        rateUnit: Int = 0,
        compactMode: Boolean = false,
        hideKiloDecimals: Boolean = true,
        isBlank: Boolean = false,
        isTransparentIcon: Boolean = false,
        forceLiveUpdateOnLockscreen: Boolean = false,
        fontPreset: Int = 0,
        customFontPath: String = ""
    ): Notification? {
        val state = createRenderState(
            speed = speed,
            isLiveUpdate = isLiveUpdate,
            isNotificationEnabled = isNotificationEnabled,
            textUp = textUp,
            textDown = textDown,
            upFirst = upFirst,
            displayMode = displayMode,
            iconMode = iconMode,
            nowbarShowArrows = nowbarShowArrows,
            nowbarExpandedMode = nowbarExpandedMode,
            textSize = textSize,
            unitSize = unitSize,
            threshold = threshold,
            lowTrafficMode = lowTrafficMode,
            useCustomColor = useCustomColor,
            color = color,
            speedUnit = speedUnit,
            minSpeedUnit = minSpeedUnit,
            rateUnit = rateUnit,
            compactMode = compactMode,
            hideKiloDecimals = hideKiloDecimals,
            isBlank = isBlank,
            isTransparentIcon = isTransparentIcon,
            forceLiveUpdateOnLockscreen = forceLiveUpdateOnLockscreen,
            fontPreset = fontPreset,
            customFontPath = customFontPath
        )

        if (state == lastRenderState) {
            return null
        }
        lastRenderState = state
        return buildNotificationFromState(state, lastPostedAtMillis)
    }

    fun buildNotification(
        speed: NetSpeedData,
        isLiveUpdate: Boolean,
        isNotificationEnabled: Boolean,
        textUp: String,
        textDown: String,
        upFirst: Boolean,
        displayMode: Int,
        iconMode: Int = 0,
        nowbarShowArrows: Boolean = false,
        nowbarExpandedMode: Int = 0,
        textSize: Float = 0.60f,
        unitSize: Float = 0.45f,
        threshold: Long = 0L,
        lowTrafficMode: Int = 0,
        useCustomColor: Boolean = false,
        color: Int = 0,
        speedUnit: String = "0",
        minSpeedUnit: Int = 0,
        rateUnit: Int = 0,
        compactMode: Boolean = false,
        hideKiloDecimals: Boolean = true,
        isBlank: Boolean = false,
        isTransparentIcon: Boolean = false,
        forceLiveUpdateOnLockscreen: Boolean = false,
        fontPreset: Int = 0,
        customFontPath: String = ""
    ): Notification {
        lastPostedAtMillis = System.currentTimeMillis()
        val state = createRenderState(
            speed = speed,
            isLiveUpdate = isLiveUpdate,
            isNotificationEnabled = isNotificationEnabled,
            textUp = textUp,
            textDown = textDown,
            upFirst = upFirst,
            displayMode = displayMode,
            iconMode = iconMode,
            nowbarShowArrows = nowbarShowArrows,
            nowbarExpandedMode = nowbarExpandedMode,
            textSize = textSize,
            unitSize = unitSize,
            threshold = threshold,
            lowTrafficMode = lowTrafficMode,
            useCustomColor = useCustomColor,
            color = color,
            speedUnit = speedUnit,
            minSpeedUnit = minSpeedUnit,
            rateUnit = rateUnit,
            compactMode = compactMode,
            hideKiloDecimals = hideKiloDecimals,
            isBlank = isBlank,
            isTransparentIcon = isTransparentIcon,
            forceLiveUpdateOnLockscreen = forceLiveUpdateOnLockscreen,
            fontPreset = fontPreset,
            customFontPath = customFontPath
        )
        lastRenderState = state
        return buildNotificationFromState(state, lastPostedAtMillis)
    }

    private fun createRenderState(
        speed: NetSpeedData,
        isLiveUpdate: Boolean,
        isNotificationEnabled: Boolean,
        textUp: String,
        textDown: String,
        upFirst: Boolean,
        displayMode: Int,
        iconMode: Int,
        nowbarShowArrows: Boolean,
        nowbarExpandedMode: Int,
        textSize: Float,
        unitSize: Float,
        threshold: Long,
        lowTrafficMode: Int,
        useCustomColor: Boolean,
        color: Int,
        speedUnit: String,
        minSpeedUnit: Int,
        rateUnit: Int,
        compactMode: Boolean,
        hideKiloDecimals: Boolean,
        isBlank: Boolean,
        isTransparentIcon: Boolean,
        forceLiveUpdateOnLockscreen: Boolean,
        fontPreset: Int,
        customFontPath: String
    ): NotificationRenderState {
        var shouldLiveUpdate = isLiveUpdate || forceLiveUpdateOnLockscreen

        if (!isNotificationEnabled) {
            return NotificationRenderState(
                mode = "disabled",
                contentText = context.getString(R.string.notification_content_text),
                isBlank = isBlank,
                isTransparentIcon = isTransparentIcon,
                useCustomColor = useCustomColor,
                color = color,
                fontPreset = fontPreset,
                customFontPath = customFontPath
            )
        }

        val normalizedIconMode = if (iconMode in 0..3) iconMode else 0
        val primaryBytes = when (normalizedIconMode) {
            1 -> speed.uploadSpeed
            2 -> speed.downloadSpeed
            else -> speed.totalSpeed
        }

        if (!forceLiveUpdateOnLockscreen && primaryBytes < threshold) {
            if (lowTrafficMode == 0) {
                return NotificationRenderState(
                    mode = "threshold-static",
                    contentText = context.getString(R.string.notification_content_text_monitoring),
                    isBlank = isBlank,
                    isTransparentIcon = isTransparentIcon,
                    useCustomColor = useCustomColor,
                    color = color,
                    fontPreset = fontPreset,
                    customFontPath = customFontPath
                )
            } else {
                shouldLiveUpdate = false
            }
        }

        val upFormatted = SpeedFormatter.formatSpeedLine(
            bytes = speed.uploadSpeed,
            speedUnit = speedUnit,
            compact = compactMode,
            hideKiloDecimals = hideKiloDecimals,
            minSpeedUnit = minSpeedUnit,
            rateUnit = rateUnit
        )
        val downFormatted = SpeedFormatter.formatSpeedLine(
            bytes = speed.downloadSpeed,
            speedUnit = speedUnit,
            compact = compactMode,
            hideKiloDecimals = hideKiloDecimals,
            minSpeedUnit = minSpeedUnit,
            rateUnit = rateUnit
        )
        val totalFormatted = SpeedFormatter.formatSpeedTextForLiveUpdate(
            bytes = speed.totalSpeed,
            speedUnit = speedUnit,
            compact = compactMode,
            hideKiloDecimals = hideKiloDecimals,
            minSpeedUnit = minSpeedUnit,
            rateUnit = rateUnit
        )

        val upText = "$textUp$upFormatted"
        val downText = "$textDown$downFormatted"
        val dualText = if (upFirst) "$upText  $downText" else "$downText  $upText"

        val contentText = when (displayMode) {
            1 -> upText
            2 -> downText
            else -> dualText
        }

        if (shouldLiveUpdate) {
            val statusText = when (normalizedIconMode) {
                1 -> if (nowbarShowArrows) "$textUp$upFormatted" else upFormatted
                2 -> if (nowbarShowArrows) "$textDown$downFormatted" else downFormatted
                3 -> if (upFirst) "▲$upFormatted ▼$downFormatted" else "▼$downFormatted ▲$upFormatted"
                else -> totalFormatted
            }

            val expandedText = when (nowbarExpandedMode) {
                1 -> totalFormatted
                2 -> contentText
                else -> dualText
            }

            return NotificationRenderState(
                mode = "live-update",
                contentText = contentText,
                statusText = statusText,
                expandedText = expandedText,
                isBlank = isBlank,
                isTransparentIcon = isTransparentIcon,
                useCustomColor = useCustomColor,
                color = color,
                fontPreset = fontPreset,
                customFontPath = customFontPath
            )
        }

        val (valueStr, unitStr) = SpeedFormatter.formatSpeedText(
            bytes = primaryBytes,
            speedUnit = speedUnit,
            compact = compactMode,
            hideKiloDecimals = hideKiloDecimals,
            minSpeedUnit = minSpeedUnit,
            rateUnit = rateUnit
        )

        return NotificationRenderState(
            mode = "bitmap",
            contentText = contentText,
            valueText = valueStr,
            unitText = unitStr,
            textSize = textSize,
            unitSize = unitSize,
            isBlank = isBlank,
            isTransparentIcon = isTransparentIcon,
            useCustomColor = useCustomColor,
            color = color,
            fontPreset = fontPreset,
            customFontPath = customFontPath
        )
    }

    private fun buildNotificationFromState(
        state: NotificationRenderState,
        postedAtMillis: Long
    ): Notification {
        val intent = Intent().apply {
            setClassName(context, MainActivity::class.java.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_speed)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setWhen(postedAtMillis)
            .setShowWhen(false)

        if (state.isTransparentIcon) {
            builder.setColor(0x00000000)
        } else if (state.useCustomColor) {
            builder.setColor(state.color)
        } else {
            textPaint.alpha = 255
            unitPaint.alpha = 255
        }

        if (state.isBlank) {
            val blankView = RemoteViews(context.packageName, R.layout.notification_blank)
            builder.setCustomContentView(blankView)
                .setCustomBigContentView(blankView)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        } else {
            builder.setContentTitle(context.getString(R.string.notification_content_title))
            builder.setContentText(state.contentText)
        }

        return when (state.mode) {
            "live-update" -> {
                val samsungExtras = android.os.Bundle().apply {
                    putInt("android.ongoingActivityNoti.style", 1)
                    putString("android.ongoingActivityNoti.primaryInfo", state.statusText)
                    putString("android.ongoingActivityNoti.secondaryInfo", state.contentText)
                    putString(
                        "android.ongoingActivityNoti.nowbarPrimaryInfo",
                        context.getString(R.string.notification_content_title)
                    )
                    putString("android.ongoingActivityNoti.nowbarSecondaryInfo", state.expandedText)

                    if (state.isTransparentIcon) {
                        putInt("android.ongoingActivityNoti.chipBgColor", 0x00000000)
                    } else if (state.useCustomColor) {
                        putInt("android.ongoingActivityNoti.chipBgColor", state.color)
                    }
                }

                builder.setCategory(NotificationCompat.CATEGORY_NAVIGATION)
                builder.addExtras(samsungExtras)
                builder.setTicker(state.statusText)
                builder.setContentTitle(state.statusText)
                builder.setShortCriticalText(state.statusText)
                    .setRequestPromotedOngoing(true)

                applySamsungHack(builder.build())
            }

            "bitmap" -> {
                val tf = CustomFontManager.getAndroidTypeface(state.fontPreset, state.customFontPath)
                textPaint.typeface = tf
                unitPaint.typeface = tf

                bitmap.eraseColor(Color.TRANSPARENT)
                val cx = size / 2f
                val cyValue = size * 0.5f
                val cyUnit = size * 0.95f

                textPaint.textSize = size * state.textSize
                unitPaint.textSize = size * state.unitSize

                val maxTextWidth = size * 0.92f
                val valueWidth = textPaint.measureText(state.valueText)
                if (valueWidth > maxTextWidth && valueWidth > 0f) {
                    textPaint.textSize *= maxTextWidth / valueWidth
                }
                val unitWidth = unitPaint.measureText(state.unitText)
                if (unitWidth > maxTextWidth && unitWidth > 0f) {
                    unitPaint.textSize *= maxTextWidth / unitWidth
                }

                canvas.drawText(state.valueText, cx, cyValue, textPaint)
                canvas.drawText(state.unitText, cx, cyUnit, unitPaint)

                val smallIcon = IconCompat.createWithBitmap(bitmap)
                builder.setSmallIcon(smallIcon)
                builder.build()
            }

            else -> builder.build()
        }
    }
}
