# Core Product Requirements

> English translation of inherited Pixel Meter requirements, modified by teddybaerd. The version, defaults and original device targets below describe the upstream design, not a complete specification of the current Nowbar Meter release. Consult the repository README and current source for implemented behavior.

| Item | Original specification |
| --- | --- |
| Product name | PixelMeter |
| Version | v0.1.0 |
| Purpose | An accurate network speed indicator for Pixel and stock Android devices, with VPN traffic filtering |

## 1. Background and problem statement

With a VPN enabled, such as V2Ray, Clash or WireGuard, traditional Android speed indicators based on `TrafficStats` or `NetworkStatsManager` can add physical-interface traffic (`wlan0` / `rmnet`) to virtual-interface traffic (`tun0`). The same traffic can then be counted twice, producing an indicated speed around twice the actual rate.

PixelMeter addresses this through a **single-data-source strategy**: use `ConnectivityManager` to identify physical interfaces and read their traffic counters while excluding virtual VPN interfaces.

## 2. Core features

### 2.1 Accurate traffic statistics

- **Data source:** Android's native `TrafficStats` API together with `ConnectivityManager`.
- **Interface detection:** Identify physical Wi-Fi, cellular and Ethernet interfaces automatically.
- **VPN filtering:** Exclude virtual VPN interfaces, such as `tun0`, without requiring a user-maintained blacklist.
- **Counter aggregation:** `TrafficCounters = Sum(Physical_Interfaces_Rx/Tx)`. Speed is derived from counter changes over elapsed time.

### 2.2 Native experience

- **Design:** Material 3, Material You and Material Express styling.
- **Android support:** The original requirements targeted Android 14/15 behavior and newer API capabilities.
- **Lightweight operation:** Focus on the core functionality, without advertising and with low resource use.

### 2.3 Display options

- **First launch:** Do not immediately start a display; let the user choose notifications or the floating window.
- **Dynamic notification icon:** Render a bitmap in real time to update the status-bar icon.
- **Floating window:** Provide an independent toggle and support dragging.
- **Combined display mode:** In the original design, `Total Speed = Upload + Download`, displayed as a single combined line.

### 2.4 Tools

- **Cloudflare speed test:** Open `speed.cloudflare.com` through Chrome Custom Tabs (CCT).

## 3. Technical requirements

- **Minimum SDK:** 31 (Android 12).
- **Target SDK:** 36 (Android 16).
- **Architecture:** MVVM with a simplified clean architecture.
- **Language:** Kotlin.
- **UI:** Jetpack Compose.
- **Dependency injection:** Koin.

## 4. Detailed feature requirements

| ID | Area | Feature | Description | Priority |
| --- | --- | --- | --- | --- |
| F01 | Core service | Background foreground service | Start a `dataSync` foreground service, account for Android 14+ restrictions, support boot autostart and hiding from recent tasks. | P0 |
| F02 | Data source | Physical-interface counters | Use `ConnectivityManager` and `TrafficStats` to obtain filtered physical-interface traffic. | P0 |
| F03 | UI | Home dashboard | Show current network speed and service status. | P0 |
| F04 | UI | Notification updates | Render a bitmap and update the notification periodically; support total, upload-only and download-only modes and configurable prefixes. | P0 |
| F05 | UI | Floating window | Use a Compose overlay with configurable background/text colors, corner radius, font size, prefixes and ordering. | P1 |
| F06 | Tools | Network speed test | Launch the Cloudflare speed test through CCT. | P2 |
| F07 | System | Quick Settings tiles | Provide `TileService` shortcuts to toggle the floating window and notification display. | P1 |
| F08 | System | Live Update | Adapt the status-bar chip for Android 16+ in the original design. | P2 |

These are inherited priorities and feature descriptions. Samsung Now Bar support and the continuation's additions are documented in the main README.

## 5. DataStore preferences

The following defaults are recorded from the original requirements and may differ from the current implementation.

| Key | Type | Original default | Description |
| --- | --- | --- | --- |
| `key_live_update` | Boolean | `false` | Home-screen Live Update toggle |
| `key_notification_enabled` | Boolean | `true` | Notification toggle |
| `key_overlay_enabled` | Boolean | `false` | Floating-window toggle |
| `key_overlay_locked` | Boolean | `false` | Lock floating-window position |
| `key_auto_start_service` | Boolean | `false` | Boot autostart toggle; permissions required |
| `key_hide_from_recents` | Boolean | `false` | Hide from the recent-tasks list |
| `key_overlay_x` | Int | `100` | Floating-window X coordinate |
| `key_overlay_y` | Int | `200` | Floating-window Y coordinate |
| `key_sampling_interval` | Long | `1500` | Sampling interval in milliseconds |
| `key_overlay_bg_color` | Int | `0xCC000000` | Floating-window background color |
| `key_overlay_text_color` | Int | `0xFFFFFFFF` | Floating-window text color |
| `key_overlay_corner_radius` | Int | `8` | Floating-window corner radius in dp |
| `key_overlay_text_size` | Float | `10.0` | Floating-window font size in sp |
| `key_overlay_text_up` | String | `▲ ` | Upload prefix |
| `key_overlay_text_down` | String | `▼ ` | Download prefix |
| `key_overlay_order_up_first` | Boolean | `true` | Display upload first in the floating window |
| `key_notification_text_up` | String | `▲ ` | Notification upload prefix |
| `key_notification_text_down` | String | `▼ ` | Notification download prefix |
| `key_notification_order_up_first` | Boolean | `true` | Display upload first in the notification |
| `key_notification_display_mode` | Int | `0` | Notification display mode: 0 = total, 1 = upload, 2 = download |
| `key_notification_text_size` | Float | `0.65` | Notification icon number size |
| `key_notification_unit_size` | Float | `0.35` | Notification icon unit size |
| `key_overlay_use_default_colors` | Boolean | `false` | Use default system colors for the floating window |

## 6. Non-functional requirements

- **Power use:** Pause background speed sampling two minutes after the screen turns off and resume promptly when it turns on. The original goal was minimal additional standby power use; this is a design objective, not a measured zero-power guarantee.
- **Privacy:** Process traffic counters locally and do not upload traffic measurements. The separate GitHub update check and user-triggered external tools are described in the main README.
