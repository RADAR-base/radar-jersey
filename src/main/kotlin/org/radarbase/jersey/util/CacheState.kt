package org.radarbase.jersey.util

import java.util.concurrent.locks.Lock

class CacheState<T>(
        val cache: T,
        val mustRefresh: Boolean,
        val mayRetry: Boolean,
        private val writeLock: Lock,
        private val refresh: () -> T,
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

    inline fun <S> query(method: (T) -> S): S {
        return if (mustRefresh) {
            method(tryRefresh() ?: cache)
        } else {
            method(cache)
        }
    }

    fun tryRefresh(): T? = writeLock.tryLocked { refresh() }
}
