package org.radarbase.jersey.config

import org.radarbase.jersey.exception.mapper.UnhandledExceptionMapper
import org.radarbase.jersey.exception.mapper.WebApplicationExceptionMapper

/** Add WebApplicationException and any exception handling. */
class GeneralExceptionResourceEnhancer: JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = arrayOf(
        UnhandledExceptionMapper::class.java,
        WebApplicationExceptionMapper::class.java,
    )
}
