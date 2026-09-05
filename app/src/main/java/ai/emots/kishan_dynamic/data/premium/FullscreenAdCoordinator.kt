package ai.emots.kishan_dynamic.data.premium

import java.util.concurrent.atomic.AtomicReference

/** Prevents independently-owned full-screen ad surfaces from overlapping. */
object FullscreenAdCoordinator {
    private val showing = AtomicReference<Any?>(null)

    fun tryAcquire(owner: Any): Boolean = showing.compareAndSet(null, owner)

    fun release(owner: Any) {
        showing.compareAndSet(owner, null)
    }

    fun isShowing(): Boolean = showing.get() != null
}
