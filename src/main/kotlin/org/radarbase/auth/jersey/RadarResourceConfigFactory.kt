package org.radarbase.auth.jersey

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig

class RadarResourceConfigFactory {
    fun resources(appConfig: ApplicationResourceConfig): ResourceConfig {
        val enhancers = appConfig.createEnhancers()
        val resources = ResourceConfig()
        resources.property("jersey.config.server.wadl.disableWadl", true)
        appConfig.configureResources(resources)
        enhancers.forEach { resources.packages(*it.packages) }

        resources.register(object : AbstractBinder() {
            override fun configure() {
                appConfig.configureBinder(this)
                enhancers.forEach { it.enhance(this) }
            }
        })
        return resources
    }
}