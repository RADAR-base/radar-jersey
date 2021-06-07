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
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig

/**
 * Add utilities such as a reusable ObjectMapper and OkHttpClient to inject.
 *
 * Do not use this class if [RadarJerseyResourceEnhancer] is already being used.
 */
class MapperResourceEnhancer: JerseyResourceEnhancer {
    var mapper: ObjectMapper? = null

    private val latestMapper: ObjectMapper
        get() = mapper ?: createDefaultMapper().also { mapper = it }

    override fun ResourceConfig.enhance() {
        register(ContextResolver { latestMapper })
    }

    override fun AbstractBinder.enhance() {
        bind(latestMapper)
            .to(ObjectMapper::class.java)
            .`in`(Singleton::class.java)
    }

    companion object {
        fun createDefaultMapper(): ObjectMapper = ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(JavaTimeModule())
            .registerModule(KotlinModule(
                nullToEmptyMap = true,
                nullToEmptyCollection = true,
                nullIsSameAsDefault = true,
            ))
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}
