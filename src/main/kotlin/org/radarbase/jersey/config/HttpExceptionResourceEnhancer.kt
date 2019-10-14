package org.radarbase.jersey.config

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.internal.inject.PerThread
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.jersey.exception.mapper.DefaultJsonExceptionRenderer
import org.radarbase.jersey.exception.mapper.DefaultTextExceptionRenderer
import org.radarbase.jersey.exception.mapper.HtmlTemplateExceptionRenderer
import org.radarbase.jersey.exception.mapper.ExceptionRenderer
import org.radarbase.jersey.exception.mapper.HttpApplicationExceptionMapper
import javax.inject.Singleton

/** Add HttpApplicationException handling. This includes a HTML templating solution. */
class HttpExceptionResourceEnhancer: JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = arrayOf(
            HttpApplicationExceptionMapper::class.java)

    override fun enhanceBinder(binder: AbstractBinder) {
        binder.bind(HtmlTemplateExceptionRenderer::class.java)
                .to(ExceptionRenderer::class.java)
                .named("text/html")
                .`in`(PerThread::class.java)

        binder.bind(DefaultJsonExceptionRenderer::class.java)
                .to(ExceptionRenderer::class.java)
                .named("application/json")
                .`in`(Singleton::class.java)

        binder.bind(DefaultTextExceptionRenderer::class.java)
                .to(ExceptionRenderer::class.java)
                .named("text/plain")
                .`in`(Singleton::class.java)
    }

    override fun enhanceResources(resourceConfig: ResourceConfig) {
        resourceConfig.registerClasses(HttpApplicationExceptionMapper::class.java)
    }
}