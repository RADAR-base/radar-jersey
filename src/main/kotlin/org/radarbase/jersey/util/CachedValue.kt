/*
 *  Copyright 2020 The Hyve
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.radarbase.jersey.util

import java.time.Duration
import java.time.Instant
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

/** Set of data that is cached for a duration of time. */
class CachedValue<T>(
        /** Duration after which the cache is considered stale and should be refreshed. */
        private val refreshDuration: Duration,
        /** Duration after which the cache may be refreshed if the cache does not fulfill a certain
         * requirement. This should be shorter than [refreshDuration] to have effect. */
        private val retryDuration: Duration,
        /** How to update the cache. */
        private val supplier: () -> T
) {
    private val refreshLock = ReentrantReadWriteLock()
    private val readLock = refreshLock.readLock()
    private val writeLock = refreshLock.writeLock()

    var cache: T = supplier()
        private set

    private var nextRefresh: Instant
    private var nextRetry: Instant

    private val state: State
        get() = readLock.locked {
            val now = Instant.now()
            return State(cache,
                    now.isAfter(nextRefresh),
                    now.isAfter(nextRetry))
        }

    init {
        val now = Instant.now()
        nextRefresh = now.plus(refreshDuration)
        nextRetry = now.plus(retryDuration)
    }

    /**
     * Get the value.
     * If the cache is empty and [retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     */
    fun get(validityPredicate: (T) -> Boolean = { true }): T = state.query(validityPredicate)

    private inner class State(val cache: T, val mustRefresh: Boolean, val mayRetry: Boolean) {
        fun query(valueIsValid: (T) -> Boolean): T {
            return if (shouldRefresh(valueIsValid)
                    && writeLock.tryLock()) {
                try {
                    supplier()
                            .also {
                                this@CachedValue.cache = it
                                val now = Instant.now()
                                nextRefresh = now.plus(refreshDuration)
                                nextRetry = now.plus(retryDuration)
                            }
                } finally {
                    writeLock.unlock()
                }
            } else {
                cache
            }
        }

        private inline fun shouldRefresh(valueIsValid: (T) -> Boolean): Boolean = mustRefresh || (!valueIsValid(cache) && mayRetry)
    }

    companion object {
        inline fun <T> Lock.locked(method: () -> T): T {
            lock()
            return try {
                method()
            } finally {
                unlock()
            }
        }
    }
}
