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

/** Set of data that is cached for a duration of time. */
class CachedSet<T>(
        /** Duration after which the cache is considered stale and should be refreshed. */
        private val refreshDuration: Duration,
        /** Duration after which the cache may be refreshed if the cache does not fulfill a certain
         * requirement. This should be shorter than [refreshDuration] to have effect. */
        private val retryDuration: Duration,
        /** How to update the cache. */
        private val supplier: () -> Iterable<T>
) {
    @set:Synchronized
    private var cached: Set<T> = emptySet()
        set(value) {
            val now = Instant.now()
            field = value
            nextRefresh = now.plus(refreshDuration)
            nextRetry = now.plus(retryDuration)
        }

    private var nextRefresh: Instant = Instant.MIN
    private var nextRetry: Instant = Instant.MIN

    @get:Synchronized
    private val state: State
        get() {
            val now = Instant.now()
            return State(cached,
                now.isAfter(nextRefresh),
                now.isAfter(nextRetry))
        }

    /** Force refresh of the cache. */
    private fun refresh() = supplier.invoke().toSet()
        .also { cached = it }

    /** Whether the cache contains [value]. If it does not contain the value and [retryDuration]
     * has passed since the last try, it will update the cache and try once more. */
    fun contains(value: T) = state.query({ it.contains(value) }, { it })
    /**
     * Find a value matching [predicate].
     * If it does not contain the value and [retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     * @return value if found and null otherwise
     */
    fun find(predicate: (T) -> Boolean): T? = state.query({ it.find(predicate) }, { it != null })
    /**
     * Get the value.
     * If the cache is empty and [retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     */
    fun get(): Set<T> = state.query({ it }, { it.isNotEmpty() })

    private inner class State(val cache: Set<T>, val mustRefresh: Boolean, val mayRetry: Boolean) {
        fun <S> query(method: (Set<T>) -> S, valueIsValid: (S) -> Boolean): S {
            var result: S
            if (mustRefresh) {
                result = method(refresh())
            } else {
                result = method(cache)
                if (!valueIsValid(result) && mayRetry) {
                    result = method(refresh())
                }
            }
            return result
        }
    }
}
