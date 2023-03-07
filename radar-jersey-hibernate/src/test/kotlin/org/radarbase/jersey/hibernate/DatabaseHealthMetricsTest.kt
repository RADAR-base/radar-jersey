package org.radarbase.jersey.hibernate

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.radarbase.jersey.GrizzlyServer
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.hibernate.db.ProjectDao
import org.radarbase.jersey.hibernate.mock.MockResourceEnhancerFactory
import java.net.URI
import java.util.concurrent.TimeUnit

internal class DatabaseHealthMetricsTest {
    @Test
    fun existsTest() {
        val authConfig = AuthConfig(
                jwtResourceName = "res_jerseyTest")
        val databaseConfig = DatabaseConfig(
                managedClasses = listOf(ProjectDao::class.qualifiedName!!),
                driver = "org.h2.Driver",
                url = "jdbc:h2:mem:test",
                dialect = "org.hibernate.dialect.H2Dialect",
        )

        val resources = ConfigLoader.loadResources(MockResourceEnhancerFactory::class.java, authConfig, databaseConfig)

        val server = GrizzlyServer(URI.create("http://localhost:9091"), resources)
        server.start()

        try {
            val client = OkHttpClient()

            client.call {
                url("http://localhost:9091/health")
            }.use { response ->
                assertThat(response.isSuccessful, `is`(true))
                assertThat(
                    response.body?.string(),
                    equalTo("{\"status\":\"UP\",\"db\":{\"status\":\"UP\"}}"),
                )
            }
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun databaseDoesNotExistTest() {
        val authConfig = AuthConfig(
                jwtResourceName = "res_jerseyTest")
        val databaseConfig = DatabaseConfig(
                managedClasses = listOf(ProjectDao::class.qualifiedName!!),
                driver = "org.h2.Driver",
                url = "jdbc:h2:tcp://localhost:9999/./test.db",
                dialect = "org.hibernate.dialect.H2Dialect",
        )

        val resources = ConfigLoader.loadResources(MockResourceEnhancerFactory::class.java, authConfig, databaseConfig)

        assertThrows<IllegalStateException> { GrizzlyServer(URI.create("http://localhost:9091"), resources) }
    }


    @Test
    fun databaseIsDisabledTest() {
        val tcp = org.h2.tools.Server.createTcpServer("-tcpPort", "9999", "-baseDir", "build/resources/test", "-ifNotExists")
        tcp.start()

        val authConfig = AuthConfig(
            jwtResourceName = "res_jerseyTest",
        )
        val databaseConfig = DatabaseConfig(
            managedClasses = listOf(ProjectDao::class.qualifiedName!!),
            driver = "org.h2.Driver",
            url = "jdbc:h2:tcp://localhost:9999/./test.db",
            dialect = "org.hibernate.dialect.H2Dialect",
            healthCheckValiditySeconds = 1L,
        )

        val resources = ConfigLoader.loadResources(MockResourceEnhancerFactory::class.java, authConfig, databaseConfig)

        val server = GrizzlyServer(URI.create("http://localhost:9091"), resources)
        server.start()

        try {
            val client = OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build()


            client.call {
                url("http://localhost:9091/health")
            }.use { response ->
                assertThat(response.isSuccessful, `is`(true))
                assertThat(response.body?.string(), equalTo("{\"status\":\"UP\",\"db\":{\"status\":\"UP\"}}"))
            }

            // Disable database. Connections should now fail
            tcp.stop()
            Thread.sleep(1_000L)

            client.call {
                url("http://localhost:9091/health")
            }.use { response ->
                assertThat(response.isSuccessful, `is`(true))
                assertThat(response.body?.string(), equalTo("{\"status\":\"DOWN\",\"db\":{\"status\":\"DOWN\"}}"))
            }
        } finally {
            server.shutdown()
        }
    }
}
