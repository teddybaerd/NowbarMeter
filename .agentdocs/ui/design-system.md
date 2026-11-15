# Design System

> English translation of inherited Pixel Meter design documentation, modified by teddybaerd. This document records the original UI design; some implementation details may differ in the current Nowbar Meter source.

Pixel Meter follows Modern Android Development (MAD) guidelines and uses Jetpack Compose for its interface.

## 1. Theme and colors

### 1.1 Material Design styling

- **Style:** Material 3, Material You and Material Express.
- **Dynamic colors:**
  - The original specification called for `DynamicColors.applyToActivitiesIfAvailable(this)`.
  - Map UI colors to the system wallpaper palette for consistency with Settings and Quick Settings.
- **Primary controls:** Use standard Material 3 components such as `Scaffold`, `TopAppBar`, `Switch`, `Card` and `NavigationBar`.
- **Settings screen:**
  - Use `me.zhanghai.compose.preference` for a native-style preference list.
  - Group settings clearly:
    - **General:** Sampling interval and permission status.
    - **Background:** Battery optimization, hiding from recent tasks and background-operation settings.
    - **Notification / Overlay:** Separate configuration sections.
  - Use `com.github.skydoves:colorpicker-compose` for color selection.

## 2. Dynamic notification icon

### Rendering approach

1. **Initial state:** The original specification keeps monitoring off until the user enables it.
2. **Create a bitmap:**
   - **Combined mode:** Add upload and download rates, and draw a single line containing the combined speed, for example `5.2 M/s` in the original illustration.
   - **Single-direction mode:** Draw only upload or download speed.
3. **Draw with Canvas:** Use `Canvas` and `Paint` to center the text in the bitmap.
   - **Font size:** Adapt to the system status-bar size or provide a manual adjustment.
   - **Color:** Use white/gray appropriate to the status-bar background, or adjust for the system's light/dark mode.
4. **IconCompat / Pixel and Android 12+ considerations:**
   - **Goal:** Display a monochrome speed icon that adapts to the status-bar background like other system icons.
   - **Approach:** Generate an alpha-mask bitmap, use `Icon.createWithBitmap(bitmap)` or `IconCompat`, and use tinting or the system's small-icon tinting behavior.
   - **Fallback considered in the original design:** If automatic tinting produces incorrect output, such as a solid white square, use a static/transparent small icon and put the speed display in the large icon.
5. **Notification builder:**
   - Prefer an alpha-mask bitmap for the small icon where supported.
   - Verify readability on real devices against light, dark and wallpaper-derived backgrounds.

## 3. Floating window

### 3.1 Window type

- Use `TYPE_APPLICATION_OVERLAY`.
- Obtain `SYSTEM_ALERT_WINDOW` permission before adding the overlay.

### 3.2 Compose in WindowManager

Use a `ComposeView` as the WindowManager view root. Configure its `LifecycleOwner` and `SavedStateRegistryOwner` so the Compose lifecycle is valid.

Illustrative code from the original design:

```kotlin
val composeView = ComposeView(context).apply {
    setContent {
        PixelPulseTheme {
            OverlayContent(...)
        }
    }
}
windowManager.addView(composeView, params)
```

### 3.3 Interaction

- **Touch handling:** Capture touch events while the window is movable to support dragging; pass-through behavior depends on the window's interaction/lock state.
- **Position persistence:** Save the current `(x, y)` coordinates to DataStore at the end of a drag, and restore them the next time the window starts.
