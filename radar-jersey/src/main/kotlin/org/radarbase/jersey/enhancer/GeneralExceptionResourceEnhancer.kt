package org.radarbase.jersey.enhancer

import org.radarbase.jersey.exception.ClientAbortExceptionWriterInterceptor
import org.radarbase.jersey.exception.mapper.UnhandledExceptionMapper
import org.radarbase.jersey.exception.mapper.WebApplicationExceptionMapper

/** Add WebApplicationException and any exception handling. */
class GeneralExceptionResourceEnhancer: JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = arrayOf(
        ClientAbortExceptionWriterInterceptor::class.java,
        UnhandledExceptionMapper::class.java,
        WebApplicationExceptionMapper::class.java,
    )
}
