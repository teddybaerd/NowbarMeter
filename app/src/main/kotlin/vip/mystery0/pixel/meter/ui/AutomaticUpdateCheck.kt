package de.teddybaerd.nowbarmeter.ui

/** One automatic check per home-screen ViewModel; persisted preferences load first. */
internal class AutomaticUpdateCheck {
    private var started = false

    suspend fun run(enabled: suspend () -> Boolean, check: suspend () -> Unit) {
        if (started) return
        started = true
        if (enabled()) check()
    }
}
