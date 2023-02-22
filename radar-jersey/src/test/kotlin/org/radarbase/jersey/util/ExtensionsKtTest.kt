package org.radarbase.jersey.util

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ExtensionsKtTest {

    @Test
    fun testConcurrentAny() {
        runBlocking {
            assertTrue(listOf(1, 2, 3, 4).concurrentAny { it > 3 })
            assertFalse(listOf(1, 2, 3, 4).concurrentAny { it < 1 })
        }
    }
}
