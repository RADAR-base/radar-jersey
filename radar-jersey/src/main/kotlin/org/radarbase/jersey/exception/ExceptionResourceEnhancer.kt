package org.radarbase.jersey.exception

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.internal.inject.PerThread
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.exception.mapper.*

/** Add WebApplicationException and any exception handling. */
class ExceptionResourceEnhancer: JerseyResourceEnhancer {
    /**
     * Renderers to use, per mediatype. To use different renderers, override the renderer that
     * should be overridden.
     */
    val renderers: MutableMap<String, Class<out ExceptionRenderer>> = mutableMapOf(
        "text/html" to HtmlTemplateExceptionRenderer::class.java,
        "application/json" to DefaultJsonExceptionRenderer::class.java,
        "text/plain" to DefaultTextExceptionRenderer::class.java,
    )

    override var classes: Array<Class<*>> = arrayOf(
        ClientAbortExceptionWriterInterceptor::class.java,
        UnhandledExceptionMapper::class.java,
        WebApplicationExceptionMapper::class.java,
        HttpApplicationExceptionMapper::class.java,
        JsonProcessingExceptionMapper::class.java,
    )

    override fun AbstractBinder.enhance() {
        renderers.forEach { (name, rendererClass) ->
            bind(rendererClass)
                .to(ExceptionRenderer::class.java)
                .named(name)
                .`in`(PerThread::class.java)
        }

        bind(ExceptionRenderers::class.java)
            .to(ExceptionRenderers::class.java)
            .`in`(Singleton::class.java)
    }
}
