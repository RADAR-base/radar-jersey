package org.radarbase.jersey.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.radarbase.jersey.config.ConfigLoader.copyEnv

internal class ConfigLoaderTest {

    val testFile = javaClass.getResource("/config/test.yaml")?.file.toString()
    val config = ConfigLoader.loadConfig<Config>(testFile, emptyArray())

    @Test
    fun loadConfig() {
        assertEquals(
            NestedConfig(requiredArg = "a", optionalArg = "b"),
            config.config,
        )
    }

    @Test
    fun testEnvOverride() {
        assertEquals(
            NestedConfig(requiredArg = "overridden_required_arg", optionalArg = "overridden_optional_arg"),
            config.config.withEnv(),
        )
    }

    data class NestedConfig(
        val requiredArg: String,
        val optionalArg: String?,
    ) {
        val envVarsMock = mapOf(
            Pair("OPTIONAL_ARG", "overridden_optional_arg"),
            Pair("REQUIRED_ARG", "overridden_required_arg"),
        )

        fun withEnv(): NestedConfig = this
            .copyEnv("OPTIONAL_ARG", { envVarsMock[it] }) { copy(optionalArg = it) }
            .copyEnv("REQUIRED_ARG", { envVarsMock[it] }) { copy(requiredArg = it) }
    }

    data class Config(
        val config: NestedConfig,
    )
}
