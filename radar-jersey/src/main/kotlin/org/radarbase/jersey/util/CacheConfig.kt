package org.radarbase.jersey.util

import java.time.Duration

data class CacheConfig(
    /** Duration after which the cache is considered stale and should be refreshed. */
    val refreshDuration: Duration = Duration.ofMinutes(5),
    /** Duration after which the cache may be refreshed if the cache does not fulfill a certain
     * requirement. This should be shorter than [refreshDuration] to have effect. */
    val retryDuration: Duration = Duration.ofSeconds(30),
    /**
     * Time after the cache should have been refreshed, after which the cache is completely
     * stale and can be removed.
     */
    val staleThresholdDuration: Duration = Duration.ofMinutes(5),
    /**
     * Whether to store exceptions in cache.
     * If true, after an exception is thrown during a state refresh, that exception will be
     * rethrown on all calls until the next refresh occurs. If there is an exception, it will
     * retry the call as soon as retryDuration has passed.
     */
    val cacheExceptions: Boolean = true,
    /** Time to wait for a lock to come free when an exception is set for the cache. */
    val exceptionLockDuration: Duration = Duration.ofSeconds(2),
    /**
     * Number of simultaneous computations that may occur. Increase if the time to computation
     * is very variable.
     */
    val maxSimultaneousCompute: Int = 1,
) {
    val refreshNanos: Long = refreshDuration.toNanos()
    val retryNanos: Long = retryDuration.toNanos()
    val staleNanos: Long = refreshNanos + staleThresholdDuration.toNanos()
    val exceptionLockNanos: Long = exceptionLockDuration.toNanos()

    init {
        check(maxSimultaneousCompute > 0) { "Number of simultaneous computations must be at least 1" }
    }
}
