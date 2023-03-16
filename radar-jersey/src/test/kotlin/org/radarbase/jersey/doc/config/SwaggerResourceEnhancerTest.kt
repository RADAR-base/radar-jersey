package org.radarbase.jersey.doc.config

import okhttp3.OkHttpClient
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
import org.radarbase.jersey.util.request
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
        client.request({
            url("http://localhost:9091/openapi.json")
        }) { response ->
            assertThat(response.code, equalTo(200))
            val responseString = response.body?.string()
            assertThat(responseString, not(emptyOrNullString()))
            println(responseString)
        }
    }

    @Test
    fun retrieveOpenApiYaml() {
        client.request({
            url("http://localhost:9091/openapi.yaml")
        }) { response ->
            assertThat(response.code, equalTo(200))
            val responseString = response.body?.string()
            assertThat(responseString, not(emptyOrNullString()))
            println(responseString)
        }
    }

    @Test
    fun retrieveWadl() {
        client.request({
            url("http://localhost:9091/application.wadl")
        }) { response ->
            assertThat(response.code, equalTo(404))
        }
    }
}
