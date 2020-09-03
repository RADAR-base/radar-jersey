package org.radarbase.jersey.hibernate

import okhttp3.OkHttpClient
import okhttp3.Request
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.radarbase.jersey.GrizzlyServer
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.hibernate.db.ProjectDao
import org.radarbase.jersey.hibernate.mock.MockResourceEnhancerFactory
import java.net.URI

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

            client.newCall(Request.Builder()
                    .url("http://localhost:9091/health")
                    .build()).execute().use { response ->
                MatcherAssert.assertThat(response.isSuccessful, Matchers.`is`(true))
                MatcherAssert.assertThat(response.body?.string(), Matchers.equalTo("{\"status\":\"UP\",\"db\":{\"status\":\"UP\"}}"))
            }
        } finally {
            server.shutdown()
        }
    }
}
