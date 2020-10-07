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

    protected val state: CacheState
        get() = readLock.locked {
            val now = Instant.now()
            return CacheState(cache,
                    now.isAfter(nextRefresh),
                    now.isAfter(nextRetry))
        }

    init {
        val now = Instant.now()
        nextRefresh = now.plus(refreshDuration)
        nextRetry = now.plus(retryDuration)
    }

    /** Force refresh of the cache. Use automatic refresh instead, if possible. */
    fun refresh(): T = writeLock.locked {
        supplier().also { cache = it }
    }

    /** Force refresh of the cache if it is not locked for writing. Use automatic refresh instead, if possible. */
    fun tryRefresh(): T? = writeLock.tryLocked {
        supplier().also { cache = it }
    }

    open fun get(): T = state.get { true }

    /**
     * Get the value.
     * If the cache is empty and [retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     */
    fun get(validityPredicate: (T) -> Boolean): T = state.get(validityPredicate)

    inner class CacheState(
            val cache: T,
            val mustRefresh: Boolean,
            val mayRetry: Boolean
    ) {
        inline fun <S> query(method: (T) -> S, valueIsValid: (S) -> Boolean): S {
            return if (mustRefresh) {
                method(tryRefresh() ?: cache)
            } else {
                val result = method(cache)
                if (!valueIsValid(result) && mayRetry) {
                    tryRefresh()
                            ?.let { method(it) }
                            ?: result
                } else result
            }
        }

        inline fun get(valueIsValid: (T) -> Boolean): T {
            return if (mustRefresh || (!valueIsValid(cache) && mayRetry)) {
                tryRefresh() ?: cache
            } else cache
        }

        inline fun test(predicate: (T) -> Boolean): Boolean = when {
            mustRefresh -> predicate(tryRefresh() ?: cache)
            predicate(cache) -> true
            mayRetry -> {
                val refreshed = tryRefresh()
                refreshed != null && predicate(refreshed)
            }
            else -> false
        }
    }
}
