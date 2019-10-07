package org.radarbase.jersey.config

import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.appconfig.exception.UnhandledExceptionMapper
import org.radarbase.appconfig.exception.WebApplicationExceptionMapper

class GeneralExceptionResourceEnhancer: JerseyResourceEnhancer {
    override fun enhanceResources(resourceConfig: ResourceConfig) {
        resourceConfig.registerClasses(
                UnhandledExceptionMapper::class.java,
                WebApplicationExceptionMapper::class.java)
    }
}
