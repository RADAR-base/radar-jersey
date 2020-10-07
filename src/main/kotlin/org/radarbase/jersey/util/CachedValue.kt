package org.radarbase.jersey.util

import java.time.Duration
import java.time.Instant
import java.util.concurrent.locks.ReentrantReadWriteLock

open class CachedValue<T: Any>(
        /** Duration after which the cache is considered stale and should be refreshed. */
        private val refreshDuration: Duration,
        /** Duration after which the cache may be refreshed if the cache does not fulfill a certain
         * requirement. This should be shorter than [refreshDuration] to have effect. */
        private val retryDuration: Duration,
        /** How to update the cache. */
        private val supplier: () -> T,
        initialValue: (() -> T)? = null,
) {

    private val refreshLock = ReentrantReadWriteLock()
    private val readLock = refreshLock.readLock()
    private val writeLock = refreshLock.writeLock()

    var cache: T = initialValue?.invoke() ?: supplier()
        private set(value) {
            val now = Instant.now()
            field = value
            nextRefresh = now.plus(refreshDuration)
            nextRetry = now.plus(retryDuration)
        }

    private var nextRefresh: Instant
    private var nextRetry: Instant

    protected val state: CacheState<T>
        get() = readLock.locked {
            val now = Instant.now()
            return CacheState(cache,
                    now.isAfter(nextRefresh),
                    now.isAfter(nextRetry),
                    writeLock,
                    ::refresh)
        }

    init {
        val now = Instant.now()
        nextRefresh = now.plus(refreshDuration)
        nextRetry = now.plus(retryDuration)
    }

    /** Force refresh of the cache. Use automatic refresh instead, if possible. */
    fun refresh(): T = supplier()
            .also { cache = it }

    /**
     * Get the value.
     * If the cache is empty and [retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     */
    fun get(validityPredicate: (T) -> Boolean = { true }): T = state.get(validityPredicate)
}
