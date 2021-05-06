package org.radarbase.jersey.config

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.internal.inject.PerThread
import org.radarbase.jersey.exception.mapper.*
import jakarta.inject.Singleton

/** Add HttpApplicationException handling. This includes a HTML templating solution. */
class HttpExceptionResourceEnhancer: JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = arrayOf(
            HttpApplicationExceptionMapper::class.java)

    override fun AbstractBinder.enhance() {
        bind(HtmlTemplateExceptionRenderer::class.java)
            .to(ExceptionRenderer::class.java)
            .named("text/html")
            .`in`(PerThread::class.java)

        bind(DefaultJsonExceptionRenderer::class.java)
            .to(ExceptionRenderer::class.java)
            .named("application/json")
            .`in`(Singleton::class.java)

        bind(DefaultTextExceptionRenderer::class.java)
            .to(ExceptionRenderer::class.java)
            .named("text/plain")
            .`in`(Singleton::class.java)
    }
}
