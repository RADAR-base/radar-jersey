package org.radarbase.jersey.config

import okhttp3.OkHttpClient
import okhttp3.Request
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.OAuthHelper.Companion.bearerHeader
import org.radarbase.jersey.auth.RadarJerseyResourceEnhancerTest
import org.radarbase.jersey.mock.MockDisabledAuthResourceEnhancerFactory
import java.net.URI

internal class DisabledAuthorizationResourceEnhancerTest {
    private lateinit var client: OkHttpClient
    private lateinit var server: HttpServer

    @BeforeEach
    fun setUp() {
        val authConfig = AuthConfig(
                jwtResourceName = "res_jerseyTest")

        val resources = ConfigLoader.loadResources(MockDisabledAuthResourceEnhancerFactory::class.java, authConfig)

        server = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:9091"), resources)
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
                .url("http://localhost:9091/")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("{\"this\":\"that\"}"))
        }
    }


    @Test
    fun testAuthenticatedGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/user")
                .header("Authorization", "Bearer none")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("{\"accessToken\":\"\"}"))
        }
    }


    @Test
    fun testExistingGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/projects/a/users/b")
                .build()).execute().use { response ->

            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("{\"projectId\":\"a\",\"userId\":\"b\"}"))
        }
    }


    @Test
    fun testNonExistingGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/projects/c/users/b")
                .header("Accept", "application/json")
                .build()).execute().use { response ->

            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(404))
            assertThat(response.body?.string(), equalTo("{\"error\":\"project_not_found\",\"error_description\":\"Project c not found.\"}"))
        }
    }
}
