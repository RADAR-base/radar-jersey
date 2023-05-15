/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.enhancer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import jakarta.ws.rs.ext.ContextResolver
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig

/**
 * Add utilities such as a reusable ObjectMapper and OkHttpClient to inject.
 *
 * Do not use this class if [RadarJerseyResourceEnhancer] is already being used.
 */
class MapperResourceEnhancer : JerseyResourceEnhancer {
    var mapper: ObjectMapper? = null

    private val latestMapper: ObjectMapper
        get() = mapper ?: createDefaultMapper().also { mapper = it }

    override fun ResourceConfig.enhance() {
        register(ObjectMapperResolver())
    }

    override fun AbstractBinder.enhance() {
        bind(latestMapper)
            .to(ObjectMapper::class.java)
    }

    companion object {
        fun createDefaultMapper(): ObjectMapper = jsonMapper {
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            serializationInclusion(JsonInclude.Include.NON_NULL)
            addModule(
                kotlinModule {
                    enable(KotlinFeature.NullToEmptyMap)
                    enable(KotlinFeature.NullToEmptyCollection)
                    enable(KotlinFeature.NullIsSameAsDefault)
                },
            )
            addModule(JavaTimeModule())
            addModule(Jdk8Module())
        }
    }

    private inner class ObjectMapperResolver : ContextResolver<ObjectMapper> {
        override fun getContext(type: Class<*>?): ObjectMapper {
            return latestMapper
        }
    }
}
