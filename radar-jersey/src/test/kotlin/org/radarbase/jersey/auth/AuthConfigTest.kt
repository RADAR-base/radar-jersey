package org.radarbase.jersey.auth

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.jvm.Throws

internal class AuthConfigTest {
    @Test
    fun testEnv() {
        val config = AuthConfig(jwtResourceName = "res_test")

        setEnv(mapOf(
                "AUTH_KEYSTORE_PASSWORD" to "test",
                "MANAGEMENT_PORTAL_CLIENT_ID" to "clId",
                "MANAGEMENT_PORTAL_CLIENT_SECRET" to "clSecret",
        ))
        val newConfig = config.withEnv()
        assertThat(newConfig, not(equalTo(config)))
        assertThat(newConfig, equalTo(
                config.copy(
                        managementPortal = config.managementPortal.copy(
                                clientId = "clId",
                                clientSecret = "clSecret",
                        ),
                        jwtKeystorePassword = "test",
                )))
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(Exception::class)
    fun setEnv(newenv: Map<String, String>) {
        try {
            val processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment")
            processEnvironmentClass.putAll(null, "theEnvironment", newenv)
            processEnvironmentClass.putAll(null, "theCaseInsensitiveEnvironment", newenv)
        } catch (e: NoSuchFieldException) {
            Collections::class.java.declaredClasses
                    .filter { "java.util.Collections\$UnmodifiableMap" == it.name }
                    .forEach { it.putAll(System.getenv(), "m", newenv) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Class<*>.putAll(obj: Any?, fieldName: String, map: Map<String, String>) {
        getDeclaredField(fieldName)
                .apply { isAccessible = true }
                .let { it.get(obj) as MutableMap<String, String> }
                .putAll(map)

    }
}
