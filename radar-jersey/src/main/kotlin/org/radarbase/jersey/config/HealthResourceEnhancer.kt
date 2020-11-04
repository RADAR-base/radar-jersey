package org.radarbase.jersey.config

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.jersey.resource.HealthResource
import org.radarbase.jersey.service.HealthService
import org.radarbase.jersey.service.ImmediateHealthService
import javax.inject.Singleton

class HealthResourceEnhancer: JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = arrayOf(HealthResource::class.java)

    override fun AbstractBinder.enhance() {
        bind(ImmediateHealthService::class.java)
                .to(HealthService::class.java)
                .`in`(Singleton::class.java)
    }
}
