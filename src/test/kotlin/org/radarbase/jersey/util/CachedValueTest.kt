package org.radarbase.jersey.util

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration

internal class CachedValueTest {
    @Volatile
    var calls: Int = 0

    @BeforeEach
    fun setUp() {
        calls = 0
    }

    @Test
    fun isStale() {
        val cache = CachedValue(
                CacheConfig(
                        refreshDuration = Duration.ofMillis(10),
                        staleThresholdDuration = Duration.ofMillis(10),
                ), supplier = { listOf("something") })
        assertThat("Initial value is stale", cache.isStale, `is`(true))
        cache.get()
        assertThat("After get, cache is not stale", cache.isStale, `is`(false))
        Thread.sleep(10)
        assertThat("After refresh duration, cache is not stale", cache.isStale, `is`(false))
        Thread.sleep(10)
        assertThat("After refresh + stale duration, cache is stale", cache.isStale, `is`(true))
    }

    @Test
    fun get() {
        val cache = CachedValue(
                CacheConfig(
                        refreshDuration = Duration.ofMillis(10),
                ),
                supplier = { calls += 1; calls },
                initialValue = { calls })
        assertThat("Initial value should refresh", cache.get(), `is`(1))
        assertThat("No refresh within threshold", cache.get(), `is`(1))
        Thread.sleep(10)
        assertThat("Refresh after threshold", cache.get(), `is`(2))
        assertThat("No refresh after threshold", cache.get(), `is`(2))
    }


    @Test
    fun getInvalid() {
        val cache = CachedValue(
                CacheConfig(
                        retryDuration = Duration.ofMillis(10),
                ),
                supplier = { calls += 1; calls },
                initialValue = { calls })
        assertThat("Initial value should refresh", cache.get { it < 0 }, `is`(1))
        assertThat("No refresh within threshold", cache.get  { it < 0 }, `is`(1))
        Thread.sleep(10)
        assertThat("Refresh after threshold", cache.get { it < 0 }, `is`(2))
        assertThat("No refresh after threshold", cache.get { it < 0 }, `is`(2))
    }

    @Test
    fun getValid() {
        var calls = 0
        val cache = CachedValue(
                CacheConfig(
                        retryDuration = Duration.ofMillis(10),
                ),
                supplier = { calls += 1; calls },
                initialValue = { calls })
        assertThat("Initial value should refresh", cache.get { it >= 0 }, `is`(1))
        assertThat("No refresh within threshold", cache.get  {  it >= 0 }, `is`(1))
        Thread.sleep(10)
        assertThat("No refresh after valid value", cache.get { it >= 0 }, `is`(1))
    }

    @Test
    fun getValue() {
        val cache = CachedValue(
                supplier = { calls += 1; calls },
                initialValue = { calls })
        assertThat("Initial value is initialValue", cache.value, `is`(0))
        assertThat("Initial get calls supplier", cache.get(), `is`(1))
        assertThat("Cache does not change by calling value", cache.value, `is`(1))
    }

    @Test
    fun refresh() {
        val cache = CachedValue(
                cacheConfig = CacheConfig(
                        refreshDuration = Duration.ofMillis(10),
                ),
                supplier = { calls += 1; calls },
                initialValue = { calls },
        )
        assertThat("Initial get calls supplier", cache.get(), `is`(1))
        assertThat("Next get uses cache", cache.get(), `is`(1))
        assertThat("Refresh gets new value", cache.refresh(), `is`(2))
        assertThat("Next get uses cache", cache.get(), `is`(2))
    }

    @Test
    fun query() {
        val cache = CachedValue(
                cacheConfig = CacheConfig(
                        refreshDuration = Duration.ofMillis(20),
                        retryDuration = Duration.ofMillis(10),
                ),
                supplier = { calls += 1; calls },
                initialValue = { calls },
        )
        assertThat("Initial value should refresh", cache.query({ it + 1 }, { it > 2 }), `is`(2))
        assertThat("No refresh within threshold", cache.query({ it + 1 }, { it > 2 }), `is`(2))
        Thread.sleep(10)
        assertThat("Retry because predicate does not match", cache.query({ it + 1 }, { it > 2 }), `is`(3))
        assertThat("No refresh within threshold", cache.query({ it + 1 }, { it > 2 }), `is`(3))
        Thread.sleep(10)
        assertThat("No retry because predicate matches", cache.query({ it + 1 }, { it > 2 }), `is`(3))
        Thread.sleep(10)
        assertThat("Refresh after refresh threshold since last retry", cache.query({ it + 1 }, { it > 2 }), `is`(4))
    }


    @Test
    fun getMultithreaded() {
        val cache = CachedValue(
                cacheConfig = CacheConfig(
                        refreshDuration = Duration.ofMillis(20),
                        retryDuration = Duration.ofMillis(10),
                ),
                supplier = {
                    Thread.sleep(50L)
                    calls += 1
                    calls
                },
                initialValue = { calls },
        )

        var exception: Throwable? = null

        val thread = Thread {
            try {
                Thread.sleep(20L)
                assertThat("Get initial value while computation is ongoing", cache.get(), `is`(0))
                Thread.sleep(70L)
                assertThat("Get new update on refresh", cache.get(), `is`(2))
            } catch (ex: Throwable) {
                exception = ex
            }
        }
        thread.start()
        assertThat("Initial value should refresh", cache.get(), `is`(1))
        thread.join()
        exception?.let { throw it }
    }

    @Test
    fun getMulti3threaded() {
        val cache = CachedValue(
                cacheConfig = CacheConfig(
                        refreshDuration = Duration.ofMillis(20),
                        retryDuration = Duration.ofMillis(10),
                        maxSimultaneousCompute = 2,
                ),
                supplier = {
                    Thread.sleep(50L)
                    calls += 1
                    calls
                },
                initialValue = { calls },
        )

        var exception: Throwable? = null

        val thread1 = Thread {
            try {
                Thread.sleep(20L)
                assertThat("Also compute while computation is ongoing", cache.get(), `is`(2))
                Thread.sleep(70L)
                assertThat("Get new update on refresh", cache.get(), `is`(4))
            } catch (ex: Throwable) {
                exception = ex
            }
        }
        val thread2 = Thread {
            try {
                Thread.sleep(40L)
                assertThat("Get initial value while computation is ongoing", cache.get(), `is`(0))
                Thread.sleep(70L)
                assertThat("Get new update on refresh", cache.get(), `is`(3))
            } catch (ex: Throwable) {
                exception = ex
            }
        }
        thread1.start()
        thread2.start()
        assertThat("Initial value should refresh", cache.get(), `is`(1))
        thread1.join()
        thread2.join()
        exception?.let { throw it }
    }


    @Test
    fun throwTest() {
        val cache = CachedValue(
                cacheConfig = CacheConfig(
                        refreshDuration = Duration.ofMillis(20),
                        retryDuration = Duration.ofMillis(10),
                ),
                supplier = { calls += 1; if (calls % 2 == 0) throw IllegalStateException() else calls },
                initialValue = { calls },
        )

        assertThat(cache.get(), `is`(1))
        assertThat(cache.get(), `is`(1))
        Thread.sleep(20L)
        assertThrows<IllegalStateException> { cache.get() }
        assertThat(cache.exception, not(nullValue()))
        assertThat(cache.exception, instanceOf(IllegalStateException::class.java))
        assertThrows<Exception> { cache.get() }
        Thread.sleep(10L)
        assertThat(cache.get(), `is`(3))
    }
}
