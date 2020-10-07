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

/** Set of data that is cached for a duration of time. */
class CachedMap<K,V>(
        /** Duration after which the cache is considered stale and should be refreshed. */
        refreshDuration: Duration,
        /** Duration after which the cache may be refreshed if the cache does not fulfill a certain
         * requirement. This should be shorter than [refreshDuration] to have effect. */
        retryDuration: Duration,
        /** How to update the cache. */
        supplier: () -> Map<K,V>
): CachedValue<Map<K, V>>(refreshDuration, retryDuration, supplier, ::emptyMap) {
    /** Whether the cache contains [key]. If it does not contain the value and [retryDuration]
     * has passed since the last try, it will update the cache and try once more. */
    operator fun contains(key: K): Boolean = state.test { key in it }

    /**
     * Find a pair matching [predicate].
     * If it does not contain the value and [retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     * @return value if found and null otherwise
     */
    fun find(predicate: (K, V) -> Boolean): Pair<K, V>? = state.query({
        it.filter { e -> predicate(e.key, e.value) }
                .toList()
                .firstOrNull()
    }, { it != null })

    /**
     * Find a pair matching [predicate].
     * If it does not contain the value and [retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     * @return value if found and null otherwise
     */
    fun findValue(predicate: (V) -> Boolean): V? = state.query({
        it.filterValues(predicate)
                .toList()
                .firstOrNull()
                ?.second
    }, { it != null })

    /**
     * Get the value.
     * If the cache is empty and [retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     */
    override fun get(): Map<K, V> = get { it.isNotEmpty() }

    /**
     * Get the value.
     * If the cache is empty and [retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     */
    operator fun get(key: K): V? = state.query({ it[key] }, { it != null })
}

