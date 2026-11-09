package com.kakao.taxi.permissions

enum class ServiceStartCheckResult {
    Ready,
    MissingOverlayPermission,
    MissingNotificationPermission
}

object DisplayPermissionState {
    fun evaluateServiceStart(
        isOverlayEnabled: Boolean,
        isNotificationEnabled: Boolean,
        hasOverlayPermission: Boolean,
        hasNotificationPermission: Boolean
    ): ServiceStartCheckResult = when {
        isOverlayEnabled && !hasOverlayPermission ->
            ServiceStartCheckResult.MissingOverlayPermission

        !hasNotificationPermission && (isNotificationEnabled || !isOverlayEnabled) ->
            ServiceStartCheckResult.MissingNotificationPermission

        else -> ServiceStartCheckResult.Ready
    }
}
