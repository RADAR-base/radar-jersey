package org.radarbase.jersey.util

import java.time.Duration
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Cached value. This value is refreshed after a refresh period has elapsed, or when the
 * cache returns an undesirable result and a retry period has elapsed. Using this class ensures
 * that the underlying supplier is not taxed too much. Any exceptions during initialization will
 * not be caught.
 */
open class CachedValue<T: Any>(
        protected val cacheConfig: CacheConfig = CacheConfig(),
        /**
         * How to update the cache. If no initial value is given, this will
         * be called as initialization.
         */
        private val supplier: () -> T,
        /**
         * Initial value of the cache. The value generated by this function is
         * considered invalid, but it will still be returned if another thread
         * is already computing the new value.
         */
        initialValue: (() -> T)? = null,
) {
    constructor(
            refreshDuration: Duration,
            retryDuration: Duration,
            supplier: () -> T,
            initialValue: (() -> T)? = null,
    ) : this(
            CacheConfig(
                    refreshDuration = refreshDuration,
                    retryDuration = retryDuration,
            ),
            supplier = supplier,
            initialValue = initialValue)

    private val refreshLock = ReentrantReadWriteLock()
    private val readLock: Lock = refreshLock.readLock()
    private val writeLock = refreshLock.writeLock()
    private val computeSemaphore = Semaphore(cacheConfig.maxSimultaneousCompute)

    val exception: Exception?
        get() = readLock.locked { _exception }

    val isStale: Boolean
        get() = readLock.locked {
            System.nanoTime() >= lastUpdateNanos + cacheConfig.staleNanos
        }

    private var cache: T
    private var lastUpdateNanos: Long
    private var _exception: Exception? = null

    init {
        if (initialValue != null) {
            cache = initialValue()
            lastUpdateNanos = Long.MIN_VALUE
        } else {
            cache = supplier()
            lastUpdateNanos = System.nanoTime()
        }
    }

    val value: T
        get() = readLock.locked { cache }

    protected val state: CacheState
        get() = readLock.locked {
            return CacheState(cache, lastUpdateNanos, _exception)
        }

    /** Force refresh of the cache. Use automatic refresh instead, if possible. */
    fun refresh(currentUpdateNanos: Long? = null): T = computeSemaphore.acquired {
        doRefresh(currentUpdateNanos)
    }

    /** Force refresh of the cache if it is not locked for writing. Use automatic refresh instead, if possible. */
    fun tryRefresh(currentUpdateNanos: Long? = null, tryLockNanos: Long? = null): T? = computeSemaphore.tryAcquired(tryLockNanos) {
        doRefresh(currentUpdateNanos)
    }

    private fun doRefresh(currentUpdateNanos: Long?): T = try {
        // Do not actually refresh if there was already a previous update
        if (currentUpdateNanos != null) {
            readLock.locked {
                if (lastUpdateNanos > currentUpdateNanos) {
                    _exception?.let { throw it }
                    return cache
                }
            }
        }
        supplier()
                .also {
                    writeLock.locked {
                        cache = it
                        lastUpdateNanos = System.nanoTime()
                        _exception = null
                    }
                }
    } catch (ex: Exception) {
        if (cacheConfig.cacheExceptions) {
            writeLock.locked {
                val now = System.nanoTime()
                // don't write exception if very recently the cache was
                // updated by another thread
                if (_exception == null && now >= lastUpdateNanos + cacheConfig.retryNanos) {
                    _exception = ex
                    lastUpdateNanos = now
                }
            }
        }
        throw ex
    }

    open fun get(): T = state.get { true }

    /**
     * Get the value.
     * If the cache is empty and [CacheConfig.retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     */
    fun get(validityPredicate: (T) -> Boolean): T = state.get(validityPredicate)

    fun <S> query(method: (T) -> S, valueIsValid: (S) -> Boolean): S = state.query(method, valueIsValid)

    /** Immutable state at a point in time. */
    protected inner class CacheState(
            /** Cached value. */
            val cache: T,
            /** Time that the cache was updated. */
            val lastUpdateNanos: Long,
            /** Cached exception. */
            val exception: Exception?,
    ) {
        val mustRefresh: Boolean
        val mayRetry: Boolean

        init {
            val now = System.nanoTime()
            mustRefresh = now >= lastUpdateNanos + cacheConfig.refreshNanos
            mayRetry = now >= lastUpdateNanos + cacheConfig.retryNanos
        }

        /**
         * Checks for exceptions and throws if necessary. If no exceptions are found,
         * proceeds to call [application].
         */
        inline fun <S> applyValidState(method: (T) -> S, application: () -> S): S {
            return if (exception != null) {
                if (mustRefresh || mayRetry) {
                    method(tryRefresh(lastUpdateNanos, cacheConfig.exceptionLockNanos) ?: throw exception)
                }
                else throw exception
            } else application()
        }

        /**
         * Query the current state, applying [method]. If [valueIsValid] does not give a valid
         * result, that is, it is false, recompute the value if [mayRetry] is true. If exceptions
         * are cached as per [CacheConfig.cacheExceptions], this may return an exception from a
         * previous call.
         */
        inline fun <S> query(method: (T) -> S, valueIsValid: (S) -> Boolean): S = applyValidState(method) {
            if (mustRefresh) method(tryRefresh(lastUpdateNanos) ?: cache)
            else {
                val result = method(cache)
                val refreshed = if (!valueIsValid(result) && mayRetry) tryRefresh(lastUpdateNanos) else null
                if (refreshed != null) method(refreshed) else result
            }
        }

        /**
         * Get the current state. If [valueIsValid] does not give a valid
         * result, that is, it is false, recompute the value if [mayRetry] is true. If exceptions
         * are cached as per [CacheConfig.cacheExceptions], this may return an exception from a
         * previous call.
         */
        inline fun get(valueIsValid: (T) -> Boolean): T = applyValidState({ it }) {
            if (mustRefresh || (!valueIsValid(cache) && mayRetry)) tryRefresh(lastUpdateNanos) ?: cache
            else cache
        }

        /**
         * Test a predicate on the current state. If the result is false, recompute the value if
         * [mayRetry] is true and run the predicate on that. If exceptions
         * are cached as per [CacheConfig.cacheExceptions], this may return an exception from a
         * previous call.
         */
        inline fun test(predicate: (T) -> Boolean): Boolean = applyValidState(predicate) {
            when {
                mustRefresh -> predicate(tryRefresh(lastUpdateNanos) ?: cache)
                predicate(cache) -> true
                mayRetry -> {
                    val refreshed = tryRefresh(lastUpdateNanos)
                    refreshed != null && predicate(refreshed)
                }
                else -> false
            }
        }
    }
}