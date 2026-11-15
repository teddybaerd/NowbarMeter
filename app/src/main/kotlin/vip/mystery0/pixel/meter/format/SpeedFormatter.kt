package de.teddybaerd.nowbarmeter.format

import java.util.Locale
import kotlin.math.roundToLong

/**
 * Formats speed text consistently across every display surface (Main Dashboard,
 * Samsung Now Bar chip, Status Bar Bitmap Icon, and Floating Overlay).
 */
object SpeedFormatter {
    const val RATE_BYTES = 0
    const val RATE_BITS = 1

    private fun normalizedRateUnit(rateUnit: Int): Int =
        if (rateUnit == RATE_BITS) RATE_BITS else RATE_BYTES

    private fun normalizedBytes(bytes: Long): Long = bytes.coerceAtLeast(0L)

    private fun rateValue(bytes: Long, rateUnit: Int): Double =
        normalizedBytes(bytes).toDouble() * if (normalizedRateUnit(rateUnit) == RATE_BITS) 8.0 else 1.0

    private fun base(rateUnit: Int): Double =
        if (normalizedRateUnit(rateUnit) == RATE_BITS) 1000.0 else 1024.0

    private fun divisor(unit: Int, rateUnit: Int): Double {
        val b = base(rateUnit)
        return when (unit) {
            2 -> b
            3 -> b * b
            4 -> b * b * b
            else -> 1.0
        }
    }

    fun fullUnit(unit: Int, rateUnit: Int): String {
        val bits = normalizedRateUnit(rateUnit) == RATE_BITS
        return when (unit) {
            2 -> if (bits) "kb/s" else "KB/s"
            3 -> if (bits) "Mb/s" else "MB/s"
            4 -> if (bits) "Gb/s" else "GB/s"
            else -> if (bits) "b/s" else "B/s"
        }
    }

    private fun minimumUnitIndex(minSpeedUnit: Int): Int = when (minSpeedUnit) {
        1 -> 2
        2 -> 3
        3 -> 4
        else -> 1
    }

    private fun formatScaledValue(
        value: Double,
        unitIndex: Int,
        compact: Boolean,
        hideKiloDecimals: Boolean
    ): String {
        if (unitIndex == 1) {
            return value.roundToLong().toString()
        }
        if (unitIndex == 2 && hideKiloDecimals) {
            return "%.0f".format(Locale.ROOT, value)
        }
        if (compact) {
            return if (value >= 10.0) {
                value.roundToLong().toString()
            } else {
                "%.1f".format(Locale.ROOT, value)
            }
        }
        val pattern = when {
            value >= 100.0 -> "%.0f"
            value >= 10.0 -> "%.1f"
            else -> if (unitIndex >= 3) "%.1f" else "%.2f"
        }
        return pattern.format(Locale.ROOT, value)
    }

    /**
     * Formats speed into Pair(valueString, unitString).
     *
     * @param bytes Raw speed in bytes/second.
     * @param speedUnit Either "0" (Auto) or comma-separated allowed unit indices ("1,2,3,4").
     * @param compact Drop decimals when value >= 10.
     * @param hideKiloDecimals Hide decimals for KB/s (and kb/s) for a cleaner look (Issue #1).
     * @param minSpeedUnit Minimum unit floor in Auto mode (0 = None, 1 = KB/kb, 2 = MB/Mb, 3 = GB/Gb).
     * @param rateUnit 0 = Bytes (1024 base), 1 = Bits (1000 SI base).
     */
    fun formatSpeedText(
        bytes: Long,
        speedUnit: String = "0",
        compact: Boolean = false,
        hideKiloDecimals: Boolean = true,
        minSpeedUnit: Int = 0,
        rateUnit: Int = 0
    ): Pair<String, String> {
        val value = rateValue(bytes, rateUnit)
        val b = base(rateUnit)
        val selectedUnits = speedUnit.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        val useAuto = selectedUnits.isEmpty() || selectedUnits.contains(0)

        if (useAuto && minSpeedUnit > 0) {
            val minIdx = minimumUnitIndex(minSpeedUnit)
            if (value < divisor(minIdx, rateUnit)) {
                return "0" to fullUnit(minIdx, rateUnit)
            }
        }

        val unitToUse = if (useAuto) {
            when {
                value >= b * b * b -> 4
                value >= b * b -> 3
                value >= b -> 2
                else -> 1
            }
        } else {
            val sortedAvailable = selectedUnits.filter { it in 1..4 }.sortedDescending()
            if (sortedAvailable.isEmpty()) {
                1
            } else {
                var best = sortedAvailable.last()
                for (unit in sortedAvailable) {
                    if (value >= divisor(unit, rateUnit)) {
                        best = unit
                        break
                    }
                }
                best
            }
        }

        val scaled = value / divisor(unitToUse, rateUnit)
        val formatted = formatScaledValue(scaled, unitToUse, compact, hideKiloDecimals)
        return formatted to fullUnit(unitToUse, rateUnit)
    }

    fun formatSpeedLine(
        bytes: Long,
        speedUnit: String = "0",
        compact: Boolean = false,
        hideKiloDecimals: Boolean = true,
        minSpeedUnit: Int = 0,
        rateUnit: Int = 0
    ): String {
        val (v, u) = formatSpeedText(
            bytes = bytes,
            speedUnit = speedUnit,
            compact = compact,
            hideKiloDecimals = hideKiloDecimals,
            minSpeedUnit = minSpeedUnit,
            rateUnit = rateUnit
        )
        return "$v$u"
    }

    fun formatSpeedTextForLiveUpdate(
        bytes: Long,
        speedUnit: String = "0",
        compact: Boolean = false,
        hideKiloDecimals: Boolean = true,
        minSpeedUnit: Int = 0,
        rateUnit: Int = 0
    ): String {
        val (v, u) = formatSpeedText(
            bytes = bytes,
            speedUnit = speedUnit,
            compact = compact,
            hideKiloDecimals = hideKiloDecimals,
            minSpeedUnit = minSpeedUnit,
            rateUnit = rateUnit
        )
        return "$v$u"
    }

    fun formatThresholdInputValue(bytes: Long, rateUnit: Int): String {
        val value = normalizedBytes(bytes).toDouble() /
                if (normalizedRateUnit(rateUnit) == RATE_BITS) 125.0 else 1024.0
        return "%.3f".format(Locale.ROOT, value).trimEnd('0').trimEnd('.')
    }

    fun parseThresholdInputValue(text: String, rateUnit: Int): Long? {
        val normalizedText = text.trim()
        if (!normalizedText.matches(Regex("^(?:\\d+\\.?\\d*|\\.\\d+)$"))) return null
        val value = normalizedText.toDoubleOrNull() ?: return null
        if (!value.isFinite() || value < 0) return null
        val bytes = value * if (normalizedRateUnit(rateUnit) == RATE_BITS) 125.0 else 1024.0
        if (!bytes.isFinite() || bytes > Long.MAX_VALUE.toDouble()) return null
        return bytes.roundToLong()
    }
}
