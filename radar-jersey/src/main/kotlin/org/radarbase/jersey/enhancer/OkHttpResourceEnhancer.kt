/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.enhancer

import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import org.glassfish.jersey.internal.inject.AbstractBinder
import java.util.concurrent.TimeUnit

/**
 * Add utilities such as a reusable ObjectMapper and OkHttpClient to inject.
 *
 * Do not use this class if [RadarJerseyResourceEnhancer] is already being used.
 */
class OkHttpResourceEnhancer: JerseyResourceEnhancer {
    var client: OkHttpClient? = null

    override fun AbstractBinder.enhance() {
        bind(client ?: createDefaultClient())
            .to(OkHttpClient::class.java)
            .`in`(Singleton::class.java)
    }

    companion object {
        fun createDefaultClient(): OkHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
