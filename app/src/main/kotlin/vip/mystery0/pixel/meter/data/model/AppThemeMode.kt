package de.teddybaerd.nowbarmeter.data.model

enum class AppThemeMode(val value: Int) {
    Dynamic(0),
    Fixed(1);

    companion object {
        const val DEFAULT_THEME_COLOR: Int = 0xFF006A66.toInt()

        fun fromValue(value: Int): AppThemeMode =
            entries.firstOrNull { it.value == value } ?: Dynamic
    }
}
