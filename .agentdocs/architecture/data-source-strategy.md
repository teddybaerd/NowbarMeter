# Data Source Strategy

> English translation of inherited Pixel Meter design documentation, modified by teddybaerd. This document records the original design; implementation details may differ in Nowbar Meter. See the repository README and current source for implemented behavior.

## Overview

Pixel Meter uses a **single data source**, Android's native `TrafficStats` API, to obtain real-time network traffic counters. Reading counters for specific Wi-Fi interfaces, such as `wlan0`, and cellular interfaces makes it possible to calculate network speed without root privileges or complex Shizuku IPC.

## Core strategy: NetworkCallback cache and TrafficStats

### How it works

The design uses an **event-driven interface cache** to improve performance and reduce time skew when multiple network interfaces are active.

1. **Initialize the cache:** Register a `ConnectivityManager.NetworkCallback` when `SpeedDataSource` is initialized.
2. **Update it as networks change:**
   - In `onAvailable` / `onCapabilitiesChanged`, check the network transport.
   - Exclude `TRANSPORT_VPN` to avoid counting the same traffic twice.
   - Include `TRANSPORT_WIFI`, `TRANSPORT_CELLULAR` and `TRANSPORT_ETHERNET`.
   - Store eligible `<Network, InterfaceName>` pairs in a `ConcurrentHashMap`.
   - In `onLost`, remove the corresponding network from the cache.
3. **Read counters quickly:**
   - `getTrafficData()` no longer queries `ConnectivityManager` through IPC for each sample.
   - Instead, iterate over cached interface names, such as `wlan0` and `rmnet_data0`.
   - Use coroutines to read the interface receive/transmit counters in parallel through `TrafficStats.getRxBytes(iface)` and `TrafficStats.getTxBytes(iface)`.

### Code structure

The following is illustrative pseudocode from the inherited design, not a complete implementation:

```kotlin
private val validInterfaces = ConcurrentHashMap<Network, String>()

// Register the callback during initialization.
init {
    connectivityManager.registerNetworkCallback(request, object : NetworkCallback() {
        override fun onCapabilitiesChanged(network, caps) {
            if (isPhysical(caps) && !isVpn(caps)) {
                validInterfaces[network] = linkProps.interfaceName
            }
        }
        override fun onLost(network) {
            validInterfaces.remove(network)
        }
    })
}

// Read cached interfaces without per-sample ConnectivityManager IPC.
override suspend fun getTrafficData() = validInterfaces.values.map { iface ->
    async { TrafficStats.getRxBytes(iface) }
}.awaitAll().sum()
```

### Benefits

1. **Less IPC overhead:** Remove repeated `getNetworkCapabilities` and `getLinkProperties` calls from the sampling loop.
2. **Reduced sampling skew:** Parallel counter reads narrow the sampling window and reduce timing differences between interfaces.
3. **Lower system overhead:** Avoid repeatedly polling SystemServer.
4. **No root or ADB requirement:** Preserve the permission advantages of the standard Android API approach.

## Historical approaches that were removed

### 1. NetworkStatsManager

- **Problem:** System-level buckets could be archived with delays of two to three hours, making them unsuitable for a live speed indicator. This produced patterns such as zero, zero, then a very large spike.
- **Attempted mitigation:** Smoothing over a two-second sampling window was tried, but the display still lagged.

### 2. Shizuku (Binder IPC)

- **Problem:** Users had to activate Shizuku, increasing setup complexity.
- **Status:** After the physical-interface `TrafficStats` approach was validated, Shizuku mode was removed to simplify the architecture.

## Original compatibility target

- **Minimum SDK:** 31 (Android 12).
- **Devices:** Primarily Google Pixel devices, plus other Android devices using standard interface names.

Nowbar Meter adapts this upstream foundation for Samsung devices; its current compatibility notes are in the repository README.
