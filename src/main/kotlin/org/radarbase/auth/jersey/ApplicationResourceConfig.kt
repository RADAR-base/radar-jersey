package org.radarbase.auth.jersey

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig

interface ApplicationResourceConfig {
    fun createEnhancers(): List<JerseyResourceEnhancer>
    fun configureResources(resources: ResourceConfig)
    fun configureBinder(binder: AbstractBinder) = Unit
}