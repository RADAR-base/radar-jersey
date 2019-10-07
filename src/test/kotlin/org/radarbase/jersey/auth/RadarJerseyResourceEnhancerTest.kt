/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth

import okhttp3.OkHttpClient
import okhttp3.Request
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.jersey.auth.OAuthHelper.Companion.bearerHeader
import org.radarbase.jersey.config.*
import org.radarbase.jersey.mock.MockResourceEnhancer
import java.net.URI

internal class RadarJerseyResourceEnhancerTest {
    private lateinit var client: OkHttpClient
    private lateinit var server: HttpServer

    @BeforeEach
    fun setUp() {
        val authConfig = AuthConfig(
                managementPortalUrl = "http://localhost:8080",
                jwtResourceName = "res_ManagementPortal")

        val enhancers = listOf(
                MockResourceEnhancer(),
                RadarJerseyResourceEnhancer(authConfig),
                ManagementPortalResourceEnhancer(),
                HttpExceptionResourceEnhancer(),
                GeneralExceptionResourceEnhancer())

        val resourceConfig = RadarResourceConfigFactory().resources(enhancers)
        server =  GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:9091"), resourceConfig)
        server.start()

        client = OkHttpClient()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun helperTest() {
        assertThat(oauthHelper, not(nullValue()))
        val token = oauthHelper.tokenValidator.validateAccessToken(oauthHelper.validEcToken)
        assertThat(token, not(nullValue()))
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
                .bearerHeader(oauthHelper)
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("{\"accessToken\":\"${oauthHelper.validEcToken}\"}"))
        }
    }


    @Test
    fun testUnauthenticatedGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/user")
                .header("Accept", "application/json")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(401))
            assertThat(response.body?.string(), equalTo("{\"error\":\"token_missing\",\"error_description\":\"No bearer token is provided in the request.\"}"))
        }
    }


    @Test
    fun testBadAuthenticationGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/user")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer abcdef")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(401))
            assertThat(response.body?.string(), equalTo("{\"error\":\"token_unverified\",\"error_description\":\"Cannot verify token. It may have been rendered invalid.\"}"))
        }
    }

    @Test
    fun testExistingGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/projects/a/users/b")
                .bearerHeader(oauthHelper)
                .build()).execute().use { response ->

            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("{\"projectId\":\"a\",\"userId\":\"b\"}"))
        }
    }


    @Test
    fun testNonExistingGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/projects/c/users/b")
                .bearerHeader(oauthHelper)
                .header("Accept", "application/json")
                .build()).execute().use { response ->

            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(404))
            assertThat(response.body?.string(), equalTo("{\"error\":\"project_not_found\",\"error_description\":\"Project c not found.\"}"))
        }
    }

    @Test
    fun testNonExistingGetHtml() {
        val response = client.newCall(Request.Builder()
                .url("http://localhost:9091/projects/c/users/b")
                .bearerHeader(oauthHelper)
                .header("Accept", "text/html,application/json")
                .build()).execute()

        assertThat(response.isSuccessful, `is`(false))
        assertThat(response.code, `is`(404))

        val body = response.body?.string()

        assertThat(body, containsString("<h1>Bad request (status code 404)</h1>"))
    }

    companion object {
        lateinit var oauthHelper: OAuthHelper

        @BeforeAll
        @JvmStatic
        fun setUpClass() {
            oauthHelper = OAuthHelper()
        }
    }
}
