package org.radarbase.jersey.hibernate

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.jersey.GrizzlyServer
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.hibernate.db.ProjectDao
import org.radarbase.jersey.hibernate.mock.MockResourceEnhancerFactory
import java.net.URI

internal class HibernateTest {
    private lateinit var client: OkHttpClient
    private lateinit var server: GrizzlyServer

    @BeforeEach
    fun setUp() {
        val authConfig = AuthConfig(
                jwtResourceName = "res_jerseyTest")
        val databaseConfig = DatabaseConfig(
                managedClasses = listOf(ProjectDao::class.qualifiedName!!),
                driver = "org.h2.Driver",
                url = "jdbc:h2:mem:test",
                dialect = "org.hibernate.dialect.H2Dialect",
        )

        val resources = ConfigLoader.loadResources(MockResourceEnhancerFactory::class.java, authConfig, databaseConfig)

        server = GrizzlyServer(URI.create("http://localhost:9091"), resources)
        server.start()

        client = OkHttpClient()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }


    @Test
    fun testBasicGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/projects")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("[]"))
        }
    }


    @Test
    fun testMissingProject() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/projects/1")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(404))
        }
    }


    @Test
    fun testExistingGet() {
        client.newCall(Request.Builder()
                .post(object : RequestBody() {
                    override fun contentType() = "application/json".toMediaTypeOrNull()

                    override fun writeTo(sink: BufferedSink) {
                        sink.writeUtf8("{\"name\": \"a\"}")
                    }

                })
                .url("http://localhost:9091/projects")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("{\"id\":1000,\"name\":\"a\",\"description\":null}"))
        }

        client.newCall(Request.Builder()
                .url("http://localhost:9091/projects/1000")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("{\"id\":1000,\"name\":\"a\",\"description\":null}"))
        }


        client.newCall(Request.Builder()
                .url("http://localhost:9091/projects")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("[{\"id\":1000,\"name\":\"a\",\"description\":null}]"))
        }

        client.newCall(Request.Builder()
                .post(object : RequestBody() {
                    override fun contentType() = "application/json".toMediaTypeOrNull()

                    override fun writeTo(sink: BufferedSink) {
                        sink.writeUtf8("{\"name\": \"a\",\"description\":\"d\"}")
                    }
                })
                .url("http://localhost:9091/projects/1000")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("{\"id\":1000,\"name\":\"a\",\"description\":\"d\"}"))
        }
        client.newCall(Request.Builder()
                .delete()
                .url("http://localhost:9091/projects/1000")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
        }

        client.newCall(Request.Builder()
                .url("http://localhost:9091/projects")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("[]"))
        }
    }
}
