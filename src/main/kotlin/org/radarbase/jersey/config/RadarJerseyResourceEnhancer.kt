/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.config

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.process.internal.RequestScoped
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.jwt.AuthFactory
import org.radarbase.jersey.auth.filter.AuthenticationFilter
import org.radarbase.jersey.auth.filter.AuthorizationFeature
import kotlin.apply
import kotlin.jvm.java

/**
 * Add RADAR auth to a Jersey project. This requires a {@link ProjectService} implementation to be
 * added to the Binder first.
 */
class RadarJerseyResourceEnhancer(
        private val config: AuthConfig
): JerseyResourceEnhancer {
    override fun enhanceResources(resourceConfig: ResourceConfig) {
        resourceConfig.registerClasses(
                AuthenticationFilter::class.java,
                AuthorizationFeature::class.java
        )
    }

    override fun enhanceBinder(binder: AbstractBinder) {
        binder.apply {
            bind(config)
                    .to(AuthConfig::class.java)

            // Bind factories.
            bindFactory(AuthFactory::class.java)
                    .proxy(true)
                    .proxyForSameScope(false)
                    .to(Auth::class.java)
                    .`in`(RequestScoped::class.java)
        }
    }
}
