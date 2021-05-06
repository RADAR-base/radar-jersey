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
import jakarta.inject.Singleton
import jakarta.ws.rs.ext.ContextResolver
import okhttp3.OkHttpClient
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.process.internal.RequestScoped
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.jwt.AuthFactory
import java.util.concurrent.TimeUnit

/**
 * Add utilities such as a reusable ObjectMapper and OkHttpClient to inject.
 *
 * Do not use this class if [RadarJerseyResourceEnhancer] is already being used.
 */
class UtilityResourceEnhancer: JerseyResourceEnhancer {
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

    override fun ResourceConfig.enhance() {
        register(ContextResolver { mapper })
    }

    override fun AbstractBinder.enhance() {
        bindFactory { client }
            .to(OkHttpClient::class.java)
            .`in`(Singleton::class.java)

        bindFactory { mapper }
            .to(ObjectMapper::class.java)
            .`in`(Singleton::class.java)

        // Bind factories.
        bindFactory(AuthFactory::class.java)
            .proxy(true)
            .proxyForSameScope(true)
            .to(Auth::class.java)
            .`in`(RequestScoped::class.java)
    }
}
