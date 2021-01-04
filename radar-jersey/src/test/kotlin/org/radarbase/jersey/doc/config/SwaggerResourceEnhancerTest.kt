package org.radarbase.jersey.doc.config

import okhttp3.OkHttpClient
import okhttp3.Request
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.mock.MockSwaggerResourceEnhancerFactory
import java.net.URI

class SwaggerResourceEnhancerTest {
    private lateinit var client: OkHttpClient
    private lateinit var server: HttpServer

    @BeforeEach
    fun setUp() {
        val authConfig = AuthConfig(
            jwtResourceName = "res_jerseyTest")

        val resources = ConfigLoader.loadResources(MockSwaggerResourceEnhancerFactory::class.java, authConfig)

        server = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:9091"), resources)
        server.start()

        client = OkHttpClient()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }


    @Test
    fun retrieveOpenApiJson() {
        client.newCall(Request.Builder().apply {
            url("http://localhost:9091/openapi.json")
        }.build()).execute()
            .use { response ->
                assertThat(response.code, equalTo(200))
                val responseString = response.body?.string()
                assertThat(responseString, not(isEmptyOrNullString()))
                println(responseString)
            }
    }

    @Test
    fun retrieveOpenApiYaml() {
        client.newCall(Request.Builder().apply {
            url("http://localhost:9091/openapi.yaml")
        }.build()).execute()
            .use { response ->
                assertThat(response.code, equalTo(200))
                val responseString = response.body?.string()
                assertThat(responseString, not(isEmptyOrNullString()))
                println(responseString)
            }
    }

    @Test
    fun retrieveWadl() {
        client.newCall(Request.Builder().apply {
            url("http://localhost:9091/application.wadl")
        }.build()).execute()
            .use { response ->
                assertThat(response.code, equalTo(404))
            }
    }
}
