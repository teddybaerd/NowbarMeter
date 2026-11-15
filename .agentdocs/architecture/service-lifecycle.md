# Service Lifecycle and Background Operation

> English translation of inherited Pixel Meter design documentation, modified by teddybaerd. This document records the original design; implementation details may differ in Nowbar Meter. See the current manifest and service implementation for the configured foreground-service types and actual behavior.

A stable foreground service keeps the network speed indicator available in the background and updates its display.

## 1. Service configuration

- **Class:** `NetworkMonitorService`.
- **Service category:** Foreground service.
- **Foreground-service type in the original design:** `dataSync`. Android 14+ requires an appropriate service type and its permission declarations.

The original example used:

```xml
<service
    android:name=".service.NetworkMonitorService"
    android:foregroundServiceType="dataSync"
    android:exported="false" />
```

This is a historical example, not a replacement for the current manifest.

## 2. Starting and maintaining the service

### 2.1 Startup sequence

1. The user enables monitoring on the main screen.
2. `Context.startForegroundService()` starts the service.
3. The service promptly calls `startForeground()` from its startup path to attach a persistent notification. The original design specifies a five-second window; failing to promote the service within the system deadline can cause termination or an application error.

### 2.2 Autostart after boot

- **Triggers:** Listen for `BOOT_COMPLETED` and `QUICKBOOT_POWERON` broadcasts.
- **Conditions:**
  1. Check that the user's `key_auto_start_service` preference is `true`.
  2. Check whether overlay or notification permission has been granted. If neither is available, do not start the service, to avoid an invalid foreground-service startup.
- **Implementation:** `BootReceiver` calls `startForegroundService` only after the relevant checks pass and subject to the system's startup restrictions.

### 2.3 Periodic sampling

The original design proposes Kotlin coroutine flows or a `Handler` for periodic work, with a 1,000 ms example interval. The actual sampling interval is configurable.

Each cycle:

- Obtain the current network speed from the repository.
- Render the notification bitmap.
- Update the notification through `NotificationManager`.
- Notify UI consumers, such as the floating window and home screen, through the application's state mechanism.

## 3. Android 14 (API 34) considerations

Foreground services are subject to Android's service-type, permission and background-start restrictions.

- **Permissions in the original design:** Declare `android.permission.FOREGROUND_SERVICE` and `android.permission.FOREGROUND_SERVICE_DATA_SYNC` in the manifest.
- **Startup timing:** A service started while the app is in the background may raise `ForegroundServiceStartNotAllowedException` unless an applicable exemption permits that start.
- **Design approach:** Start the service in response to a user action from the visible UI, or from the boot receiver when the system permits it. A boot broadcast is not an unconditional exemption on every Android version or for every service type.

Consult the current manifest and implementation when making changes for newer Android releases.

## 4. Releasing resources

The original design calls for stopping monitoring when the user disables the feature or uses an exit action. Call `stopForeground(STOP_FOREGROUND_REMOVE)` and `stopSelf()` as appropriate to remove the foreground notification, stop the service and release resources.

## 5. Power and performance

The inherited design includes a screen-state sleep/wake strategy to reduce continuous CPU use while the display is off.

### 5.1 Screen-state broadcasts

The service uses a `BroadcastReceiver` to observe:

- `Intent.ACTION_SCREEN_OFF`: The screen has turned off.
- `Intent.ACTION_SCREEN_ON`: The screen has turned on.

### 5.2 Sleep and wake strategy

1. **Delayed pause:** When the screen turns off, start a two-minute countdown instead of immediately stopping monitoring. This accommodates a brief press of the power button.
2. **Pause sampling:** If the screen stays off for two minutes, stop the network-monitoring coroutine through `stopMonitoring`, reducing ongoing CPU work and allowing the device to sleep.
3. **Resume promptly:** When the screen turns on:
   - If the countdown is still active, cancel it and keep monitoring.
   - If sampling has paused, call `startMonitoring` to resume updates.

The goal is to resume speed updates when the screen becomes visible while reducing standby work. This describes pausing sampling, not necessarily terminating the foreground service or removing its notification.
