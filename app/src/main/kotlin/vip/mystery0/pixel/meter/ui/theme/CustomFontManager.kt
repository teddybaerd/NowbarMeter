package com.kakao.taxi.ui.theme

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Typeface as ComposeTypeface
import java.io.File

/**
 * Manages built-in compact/monospace font presets and user-imported custom fonts (.ttf/.otf)
 * for status bar bitmap icons, floating overlays, and the main dashboard.
 */
object CustomFontManager {
    private const val TAG = "CustomFontManager"
    private const val CUSTOM_FONT_FILENAME = "custom_meter_font.ttf"

    const val PRESET_DEFAULT = 0
    const val PRESET_CONDENSED = 1
    const val PRESET_MONOSPACE = 2
    const val PRESET_SERIF = 3
    const val PRESET_CUSTOM = 4

    @Volatile
    private var cachedCustomPath: String? = null

    @Volatile
    private var cachedCustomTypeface: Typeface? = null

    fun getAndroidTypeface(preset: Int, customFontPath: String = ""): Typeface {
        return when (preset) {
            PRESET_CONDENSED -> Typeface.create("sans-serif-condensed", Typeface.BOLD)
            PRESET_MONOSPACE -> Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            PRESET_SERIF -> Typeface.create(Typeface.SERIF, Typeface.BOLD)
            PRESET_CUSTOM -> loadCustomTypeface(customFontPath) ?: Typeface.DEFAULT_BOLD
            else -> Typeface.DEFAULT_BOLD
        }
    }

    fun getComposeFontFamily(preset: Int, customFontPath: String = ""): FontFamily {
        return when (preset) {
            PRESET_CONDENSED -> FontFamily(
                ComposeTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD))
            )
            PRESET_MONOSPACE -> FontFamily.Monospace
            PRESET_SERIF -> FontFamily.Serif
            PRESET_CUSTOM -> {
                val tf = loadCustomTypeface(customFontPath)
                if (tf != null) FontFamily(ComposeTypeface(tf)) else FontFamily.Default
            }
            else -> FontFamily.Default
        }
    }

    private fun loadCustomTypeface(path: String): Typeface? {
        if (path.isBlank()) return null
        val cached = cachedCustomTypeface
        if (path == cachedCustomPath && cached != null) {
            return cached
        }
        return try {
            val file = File(path)
            if (!file.isFile || !file.canRead()) return null
            val raw = Typeface.createFromFile(file)
            val bold = Typeface.create(raw, Typeface.BOLD)
            cachedCustomPath = path
            cachedCustomTypeface = bold
            bold
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load custom font from $path", e)
            null
        }
    }

    /**
     * Copies a user-selected font URI (.ttf/.otf) into app internal storage and validates it.
     * @return Pair(internalFilePath, displayName) if valid, or null on failure.
     */
    fun importCustomFont(context: Context, uri: Uri): Pair<String, String>? {
        return try {
            val displayName = queryDisplayName(context, uri) ?: CUSTOM_FONT_FILENAME
            val targetFile = File(context.filesDir, CUSTOM_FONT_FILENAME)
            context.contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            // Validate that Android's font engine can parse this file
            val tf = Typeface.createFromFile(targetFile)
            if (tf == null || tf == Typeface.DEFAULT) {
                // Still allow if createFromFile didn't throw
            }
            cachedCustomPath = targetFile.absolutePath
            cachedCustomTypeface = Typeface.create(tf, Typeface.BOLD)
            targetFile.absolutePath to displayName
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import custom font", e)
            null
        }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) cursor.getString(idx) else null
                    } else null
                }
        } catch (_: Exception) {
            null
        }
    }
}
