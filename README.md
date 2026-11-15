# Continuation and test status

This fork preserves realMoai's NowbarMeter v1.3, via joelsubash2000's fork, and the original Pixel Meter attribution and Apache-2.0 license.

The first test uses the independent application ID `de.teddybaerd.nowbarmeter` and can coexist with the original app. Samsung Now Bar support with this ID is unverified. Stop the original monitor before testing to distinguish notifications.

Two flavors share the same source: `standard` (own identity) and `compat` (legacy `com.kakao.taxi` identity). Compat requires uninstalling an original app signed with a different key. Only standard is built by the test workflow.

Modified files in the continuation change package/namespace identity, update links, build variants and release automation. No APK from this fork has yet been validated on a physical device.

## First device test

Install the standard test artifact. Grant notification permission, enable monitoring and Now Bar, and check status bar, expanded card and lockscreen. Check with screen off, after reboot, and with VPN enabled. Record Android/One UI versions and Bitdefender's result. Compare against the original v1.3 with only one monitor running at a time.

## Signing

Release builds require a persistent signing key configured through repository secrets. Never generate a replacement key for every build. Debug artifacts are disposable tests; their signing identity is not a promised upgrade path.

---

# Nowbar Meter

<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="200" alt="Nowbar Meter Logo"/>
</p>

<p align="center">
  <strong>Precise internet speed monitor hijacked directly into the Samsung One UI Now Bar.</strong>
</p>

<p align="center">
    <a href="LICENSE"><img src="https://img.shields.io/github/license/teddybaerd/NowbarMeter" alt="License"></a>
    <img src="https://img.shields.io/badge/Version-1.3-green" alt="Version 1.3">
    <img src="https://img.shields.io/badge/Forked_From-Pixel_Meter-blue" alt="Forked from Pixel Meter">
    <img src="https://img.shields.io/github/downloads/teddybaerd/NowbarMeter/total" alt="Downloads">
    <a href="https://buymeacoffee.com/nowbarmeter"><img src="https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?style=flat&logo=buy-me-a-coffee&logoColor=black" alt="Buy Me A Coffee"></a>
</p>

## About

NowbarMeter is a specialized fork of [Pixel Meter](https://github.com/Pixel-Tailor-CN/PixelMeter), rebuilt specifically for Samsung Galaxy devices.

Samsung severely restricted the **Now Bar** (Live Updates / Status Bar Chip) feature in modern One UI builds, locking it behind a hardcoded system whitelist of proprietary and partner apps. **NowbarMeter bypasses this restriction.** By injecting the `com.samsung.android.support.ongoing_activity` metadata flag and strategically spoofing a whitelisted package name (`com.kakao.taxi`), this app forces a native, real-time network speed monitor directly into the One UI Now Bar pill.

It retains all the features of the original app, including intelligent VPN traffic filtering, so your displayed speed is exactly what your physical interface is actually pulling.

## Screenshots

<div align="center">
  <img src="docs/FULL_SCREEN_1.jpg" width="45%" style="margin-right: 2%;" alt="Home Screen"/>
  <img src="docs/FULL_SCREEN_2.jpg" width="45%" alt="Settings Screen"/>
</div>
<br>
<div align="center">
  <img src="docs/SLIM_STATUS_BAR_CHIP.jpg" width="95%" alt="Now Bar Integration"/>
  <p><em>Native integration directly into the One UI status bar.</em></p>
</div>

## Installation

Since this app modifies system-level UI elements, it is not available on the Google Play Store.

1. Go to the [Releases Page](https://github.com/teddybaerd/NowbarMeter/releases/latest).
2. Download the latest `NowbarMeter-vX.X.apk` file under the **Assets** dropdown.
3. Open the APK file to install it.

## Features

- **Samsung Now Bar Native Integration**: Hijacks the One UI status bar chip to display real-time speeds (Total, Download Only, Upload Only, or Dual `▼ Down ▲ Up`) directly in the status bar pill, plus customizable expanded Now Bar card content.
- **One UI 7+ Payload Injection**: Bypasses Android 15/16 hardcoded signature verification by injecting proprietary Samsung metadata and `semFlags` into the notification builder.
- **Lockscreen Override**: Uses system broadcast receivers to force the Live Update chip to render on your secure lockscreen, even if you have it hidden in the regular status bar.
- **Custom Fonts & Presets**: Choose from compact built-in presets (*Sans-Serif Condensed*, *Monospace Tabular*, *Serif*) or import any custom `.ttf`/`.otf` font file for the status bar icon, floating overlay, and dashboard.
- **Flexible Units & Clean Formatting**:
  - Toggle between **Bytes (`B/s`, `KB/s`, `MB/s`, `GB/s`)** and **Bits (`b/s`, `kb/s`, `Mb/s`, `Gb/s`)**.
  - **Hide KB/s Decimals** option (`8 KB/s` vs `8.2 MB/s`) for instant readability at a glance.
  - **Minimum Display Unit** floor to suppress tiny background traffic fluctuations in Auto mode.
  - **Compact Speed Text**: Dynamically drops decimals on higher speeds (`>= 10`) so text never overflows.
- **Advanced Floating Window (Overlay)**: Supports horizontal or vertical stacked layout, precise X/Y coordinates, drawing over the status bar, transparent background, and smart auto-hide in **Landscape**, **Immersive/Fullscreen mode**, or during **Low Traffic**.
- **Built-In OTA Updater**: Checks GitHub Releases on launch with release notes preview and direct APK download.
- **Ultra-Lightweight APK (~1.7 MB)**: R8-minified and resource-shrunk release build (down from 62.8 MB).
- **Precise Traffic Stats**: Uses `ConnectivityManager` and `TrafficStats` with monotonic clock sampling to filter out `tun0` and virtual VPN interfaces without reconnect speed spikes.

## Requirements

- **Device**: Samsung Galaxy devices running One UI 7.0 or higher.
- **Android Version**: Android 12 (API Level 31) or higher.
- **Permissions**: Notification (for the Now Bar / status bar monitor) or Display over other apps (for the Floating Window), plus Battery Optimization bypass.

## Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture Pattern**: MVVM + Clean Architecture
- **Dependency Injection**: Koin
- **Data Source**: `TrafficStats` + `ConnectivityManager`

## Support

If you find this app useful and want to help support the reverse-engineering and late nights spent fighting Samsung's UI restrictions, consider buying me a coffee! ☕

<a href="https://buymeacoffee.com/nowbarmeter" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 50px !important;width: 181px !important;" ></a>

## Credits

Massive shoutout to [Mystery00](https://github.com/Pixel-Tailor-CN/PixelMeter) for building the original, open-source [Pixel Meter](https://github.com/Pixel-Tailor-CN/PixelMeter). This fork adapts their networking logic and feature set to break through Samsung's UI restrictions.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
