package org.radarbase.jersey.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ConfigLoaderTest {
    @Test
    fun loadConfig() {
        val testFile = javaClass.getResource("/config/test.yaml")?.file.toString()
        val config = ConfigLoader.loadConfig<Config>(testFile, emptyArray())
        assertEquals(Config(mapOf("test" to "a")), config)
    }

    data class Config(val config: Map<String, String>)
}
