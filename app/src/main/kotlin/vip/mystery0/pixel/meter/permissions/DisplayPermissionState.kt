// Modified by teddybaerd for the Nowbar Meter continuation (2026): independent app identity, localization, privacy controls and release maintenance.
package de.teddybaerd.nowbarmeter.permissions

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
