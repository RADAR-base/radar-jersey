/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.config

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.process.internal.RequestScoped
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.filter.AuthenticationFilter
import org.radarbase.jersey.auth.filter.AuthorizationFeature
import org.radarbase.jersey.auth.jwt.AuthFactory

/**
 * Add RADAR auth to a Jersey project. This requires a {@link ProjectService} implementation to be
 * added to the Binder first.
 */
class RadarJerseyResourceEnhancer(
    private val config: AuthConfig,
): JerseyResourceEnhancer {
    /**
     * Utilities. Set to `null` to avoid injection. Modify utility mapper or client to inject
     * a different mapper or client.
     */
    var utilityResourceEnhancer: UtilityResourceEnhancer? = UtilityResourceEnhancer()

    override val classes = arrayOf(
        AuthenticationFilter::class.java,
        AuthorizationFeature::class.java,
    )

    override fun ResourceConfig.enhance() {
        utilityResourceEnhancer?.enhanceResources(this)
    }

    override fun AbstractBinder.enhance() {
        bindFactory { config.withEnv() }
            .to(AuthConfig::class.java)
            .`in`(Singleton::class.java)

        // Bind factories.
        bindFactory(AuthFactory::class.java)
            .proxy(true)
            .proxyForSameScope(true)
            .to(Auth::class.java)
            .`in`(RequestScoped::class.java)

        utilityResourceEnhancer?.enhanceBinder(this)
    }
}
