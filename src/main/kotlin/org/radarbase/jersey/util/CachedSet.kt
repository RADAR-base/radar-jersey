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
class CachedSet<T>(
        /** Duration after which the cache is considered stale and should be refreshed. */
        refreshDuration: Duration,
        /** Duration after which the cache may be refreshed if the cache does not fulfill a certain
         * requirement. This should be shorter than [refreshDuration] to have effect. */
        retryDuration: Duration,
        /** How to update the cache. */
        supplier: () -> Set<T>
): CachedValue<Set<T>>(refreshDuration, retryDuration, supplier, ::emptySet) {
    /** Whether the cache contains [value]. If it does not contain the value and [retryDuration]
     * has passed since the last try, it will update the cache and try once more. */
    operator fun contains(value: T): Boolean = state.test { value in it }

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
    override fun get(): Set<T> = get { it.isNotEmpty() }
}
