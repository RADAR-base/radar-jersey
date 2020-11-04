/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.OkHttpClient
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.process.internal.RequestScoped
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.filter.AuthenticationFilter
import org.radarbase.jersey.auth.filter.AuthorizationFeature
import org.radarbase.jersey.auth.jwt.AuthFactory
import java.util.concurrent.TimeUnit
import javax.ws.rs.ext.ContextResolver

/**
 * Add RADAR auth to a Jersey project. This requires a {@link ProjectService} implementation to be
 * added to the Binder first.
 */
class RadarJerseyResourceEnhancer(
        private val config: AuthConfig
): JerseyResourceEnhancer {
    var mapper: ObjectMapper = ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(JavaTimeModule())
            .registerModule(KotlinModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    var client: OkHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    override val classes = arrayOf(
            AuthenticationFilter::class.java,
            AuthorizationFeature::class.java)

    override fun ResourceConfig.enhance() {
        register(ContextResolver { mapper })
    }

    override fun AbstractBinder.enhance() {
        bind(config.withEnv())
                .to(AuthConfig::class.java)

        bind(client)
                .to(OkHttpClient::class.java)

        bind(mapper)
                .to(ObjectMapper::class.java)

        // Bind factories.
        bindFactory(AuthFactory::class.java)
                .proxy(true)
                .proxyForSameScope(true)
                .to(Auth::class.java)
                .`in`(RequestScoped::class.java)
    }
}
