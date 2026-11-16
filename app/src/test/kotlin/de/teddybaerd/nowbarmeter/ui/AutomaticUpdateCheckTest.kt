package de.teddybaerd.nowbarmeter.ui

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Test

class AutomaticUpdateCheckTest {
    @Test fun disabledPreferenceNeverRequestsUpdate() = runBlocking {
        var requests = 0
        AutomaticUpdateCheck().run(enabled = { false }) { requests++ }
        assertEquals(0, requests)
    }

    @Test fun waitsForStoredPreferenceInsteadOfUsingDefault() = runBlocking {
        val persisted = CompletableDeferred<Boolean>()
        var requests = 0
        val job = launch { AutomaticUpdateCheck().run(enabled = { persisted.await() }) { requests++ } }
        yield()
        assertEquals(0, requests)
        persisted.complete(false)
        job.join()
        assertEquals(0, requests)
    }

    @Test fun recompositionAndPreferenceChangesDoNotRepeatAutomaticCheck() = runBlocking {
        val policy = AutomaticUpdateCheck()
        var requests = 0
        repeat(3) { policy.run(enabled = { true }) { requests++ } }
        assertEquals(1, requests)
    }
}
