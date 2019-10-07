package org.radarbase.jersey.config

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig

class RadarResourceConfigFactory {
    fun resources(enhancers: List<JerseyResourceEnhancer>): ResourceConfig {
        val resources = ResourceConfig()
        resources.property("jersey.config.server.wadl.disableWadl", true)
        enhancers.forEach { it.enhanceResources(resources) }

        resources.register(object : AbstractBinder() {
            override fun configure() {
                enhancers.forEach { it.enhanceBinder(this) }
            }
        })
        return resources
    }
}