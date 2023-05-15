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
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.process.internal.RequestScoped
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.AuthService
import org.radarbase.jersey.auth.filter.AuthenticationFilter
import org.radarbase.jersey.auth.filter.AuthorizationFeature
import org.radarbase.jersey.auth.jwt.RadarTokenFactory
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.jersey.service.ScopedAsyncCoroutineService

/**
 * Add RADAR auth to a Jersey project. This requires a {@link ProjectService} implementation to be
 * added to the Binder first.
 *
 * @param includeMapper is set, this also instantiates [MapperResourceEnhancer].
 */
class RadarJerseyResourceEnhancer(
    private val config: AuthConfig,
    includeMapper: Boolean = true,
) : JerseyResourceEnhancer {
    /**
     * Utilities. Set to `null` to avoid injection. Modify utility mapper or client to inject
     * a different mapper or client.
     */
    private val mapperResourceEnhancer: MapperResourceEnhancer? = if (includeMapper) MapperResourceEnhancer() else null

    override val classes = arrayOf(
        AuthenticationFilter::class.java,
        AuthorizationFeature::class.java,
    )

    override fun ResourceConfig.enhance() {
        register(JacksonFeature.withoutExceptionMappers())
        mapperResourceEnhancer?.enhanceResources(this)
    }

    override fun AbstractBinder.enhance() {
        bind(config.withEnv())
            .to(AuthConfig::class.java)
            .`in`(Singleton::class.java)

        bind(ScopedAsyncCoroutineService::class.java)
            .to(AsyncCoroutineService::class.java)
            .`in`(Singleton::class.java)

        // Bind factories.
        bindFactory(RadarTokenFactory::class.java)
            .to(RadarToken::class.java)
            .`in`(RequestScoped::class.java)

        bind(AuthService::class.java)
            .to(AuthService::class.java)
            .`in`(Singleton::class.java)

        mapperResourceEnhancer?.enhanceBinder(this)
    }
}
