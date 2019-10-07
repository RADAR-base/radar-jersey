package org.radarbase.jersey.config

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.internal.inject.PerThread
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.jersey.exception.mapper.DefaultExceptionHtmlRenderer
import org.radarbase.jersey.exception.mapper.ExceptionHtmlRenderer
import org.radarbase.jersey.inject.HttpApplicationExceptionMapper

class HttpExceptionResourceEnhancer: JerseyResourceEnhancer {
    override fun enhanceBinder(binder: AbstractBinder) {
        binder.bind(DefaultExceptionHtmlRenderer::class.java)
                .to(ExceptionHtmlRenderer::class.java)
                .`in`(PerThread::class.java)
    }

    override fun enhanceResources(resourceConfig: ResourceConfig) {
        resourceConfig.registerClasses(HttpApplicationExceptionMapper::class.java)
    }
}