/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.jersey.auth.OAuthHelper.Companion.bearerHeader
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.mock.MockResourceEnhancerFactory
import java.net.URI

internal class RadarJerseyResourceEnhancerTest {
    private lateinit var oauthHelper: OAuthHelper
    private lateinit var client: OkHttpClient
    private lateinit var server: HttpServer

    @BeforeEach
    fun setUp() {
        val authConfig = AuthConfig(
            managementPortal = MPConfig(url = "http://localhost:8080"),
            jwtResourceName = "res_ManagementPortal",
        )

        val resources = ConfigLoader.loadResources(MockResourceEnhancerFactory::class.java, authConfig)

        server = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:9091"), resources)
        server.start()

        client = OkHttpClient()
        oauthHelper = OAuthHelper()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun helperTest() {
        assertThat(oauthHelper, not(nullValue()))
        val token = oauthHelper.tokenValidator.validateBlocking(oauthHelper.validEcToken)
        assertThat(token, not(nullValue()))
    }

    @Test
    fun testBasicGet() {
        client.request(
            "http://localhost:9091/",
            callback = { response ->
                assertThat(response.isSuccessful, `is`(true))
                assertThat(response.body?.string(), equalTo("{\"this\":\"that\"}"))
            },
        )
    }

    @Test
    fun testAuthenticatedGet() {
        client.request(
            "http://localhost:9091/user",
            buildRequest = {
                bearerHeader(oauthHelper)
            },
            callback = { response ->
                assertThat(response.isSuccessful, `is`(true))
                assertThat(response.body?.string(), equalTo("""{"accessToken":"${oauthHelper.validEcToken}"}"""))
            },
        )
    }

    @Test
    fun testAuthenticatedGetDetailed() {
        client.newCall(
            Request.Builder()
                .url("http://localhost:9091/user/detailed")
                .bearerHeader(oauthHelper)
                .build(),
        ).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("""{"accessToken":"${oauthHelper.validEcToken}","name":"name","createdAt":"1970-01-01T01:00:00Z"}"""))
        }
    }

    @Test
    fun testAuthenticatedPostDetailed() {
        client.newCall(
            Request.Builder()
                .url("http://localhost:9091/user")
                .bearerHeader(oauthHelper)
                .post("""{"accessToken":"${oauthHelper.validEcToken}","name":"name","createdAt":"1970-01-01T01:00:00Z"}""".toRequestBody("application/json".toMediaType()))
                .build(),
        ).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("""{"accessToken":"${oauthHelper.validEcToken}","name":"name","createdAt":"1970-01-01T01:00:00Z"}"""))
        }
    }

    @Test
    fun testAuthenticatedPostDetailedBadRequest() {
        client.newCall(
            Request.Builder()
                .url("http://localhost:9091/user")
                .bearerHeader(oauthHelper)
                .post("""{}""".toRequestBody("application/json".toMediaType()))
                .build(),
        ).execute().use { response ->
            assertThat(response.code, `is`(400))
        }
    }

    @Test
    fun testUnauthenticatedGet() {
        client.newCall(
            Request.Builder()
                .url("http://localhost:9091/user")
                .header("Accept", "application/json")
                .build(),
        ).execute().use { response ->
            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(401))
            assertThat(response.body?.string(), equalTo("{\"error\":\"token_missing\",\"error_description\":\"No bearer token is provided in the request.\"}"))
        }
    }

    @Test
    fun testUnauthenticatedGetNoAcceptHeader() {
        client.newCall(
            Request.Builder()
                .url("http://localhost:9091/user")
                .header("Accept", "*/*")
                .build(),
        ).execute().use { response ->
            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(401))
            assertThat(response.body?.string(), equalTo("{\"error\":\"token_missing\",\"error_description\":\"No bearer token is provided in the request.\"}"))
        }
    }

    @Test
    fun testBadAuthenticationGet() {
        client.newCall(
            Request.Builder()
                .url("http://localhost:9091/user")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer abcdef")
                .build(),
        ).execute().use { response ->
            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(401))
            assertThat(response.body?.string(), equalTo("{\"error\":\"token_unverified\",\"error_description\":\"Cannot verify token. It may have been rendered invalid.\"}"))
        }
    }

    @Test
    fun testExistingGet() {
        client.newCall(
            Request.Builder()
                .url("http://localhost:9091/projects/a/users/b")
                .bearerHeader(oauthHelper)
                .build(),
        ).execute().use { response ->

            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("{\"projectId\":\"a\",\"userId\":\"b\"}"))
        }
    }

    @Test
    fun testNonExistingGet() {
        client.newCall(
            Request.Builder()
                .url("http://localhost:9091/projects/c/users/b")
                .bearerHeader(oauthHelper)
                .header("Accept", "application/json")
                .build(),
        ).execute().use { response ->
            assertThat(response.body?.string(), equalTo("{\"error\":\"project_not_found\",\"error_description\":\"Project c not found.\"}"))
            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(404))
        }
    }

    @Test
    fun testNonExistingGetHtml() {
        client.newCall(
            Request.Builder()
                .url("http://localhost:9091/projects/c/users/b")
                .bearerHeader(oauthHelper)
                .header("Accept", "text/html,application/json")
                .build(),
        ).execute().use { response ->

            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(404))

            val body = response.body?.string()

            assertThat(body, containsString("<h1>Bad request (status code 404)</h1>"))
        }
    }

    @Test
    fun testNonExistingGetBrowser() {
        client.newCall(
            Request.Builder()
                .url("http://localhost:9091/projects/c/users/b")
                .bearerHeader(oauthHelper)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build(),
        ).execute().use { response ->

            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(404))

            val body = response.body?.string()

            assertThat(body, containsString("<h1>Bad request (status code 404)</h1>"))
        }
    }

    @Test
    fun exceptionTest() {
        client.newCall(
            Request.Builder().apply {
                url("http://localhost:9091/exception")
                header("Accept", "application/json")
            }.build(),
        ).execute().use { response ->
            assertThat(response.code, `is`(500))
            val body = response.body?.string()
            assertThat(body, equalTo("""{"error":"unknown","error_description":"Unknown exception."}"""))
        }
    }

    @Test
    fun badRequestTest() {
        client.newCall(
            Request.Builder().apply {
                url("http://localhost:9091/badrequest")
                header("Accept", "application/json")
            }.build(),
        ).execute().use { response ->
            assertThat(response.code, `is`(400))
            val body = response.body?.string()
            assertThat(body, equalTo("""{"error":"code","error_description":"message"}"""))
        }
    }

    @Test
    fun jerseyBadRequestTest() {
        client.newCall(
            Request.Builder().apply {
                url("http://localhost:9091/jerseybadrequest")
                header("Accept", "application/json")
            }.build(),
        ).execute().use { response ->
            assertThat(response.code, `is`(400))
            val body = response.body?.string()
            assertThat(body, equalTo(""))
        }
    }

    companion object {
        private fun OkHttpClient.request(
            url: String,
            buildRequest: (Request.Builder.() -> Unit)? = null,
            callback: (Response) -> Unit,
        ) {
            val requestBuilder = Request.Builder().url(url)
            if (buildRequest != null) {
                requestBuilder.buildRequest()
            }
            newCall(requestBuilder.build()).execute().use(callback)
        }
    }
}
