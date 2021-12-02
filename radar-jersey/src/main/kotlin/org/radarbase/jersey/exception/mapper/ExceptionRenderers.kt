package org.radarbase.jersey.exception.mapper

import jakarta.inject.Singleton
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import org.glassfish.hk2.api.IterableProvider
import org.radarbase.jersey.exception.HttpApplicationException
import org.slf4j.LoggerFactory

@Singleton
@Provider
class ExceptionRenderers(
    @Context private val requestContext: ContainerRequestContext,
    @Context private val renderers: IterableProvider<ExceptionRenderer>,
) {
    private val mediaType: MediaType
        get() = requestContext.acceptableMediaTypes
            .firstOrNull { type -> type in supportedTypes }
            .takeIf { it != MediaType.WILDCARD_TYPE }
            ?: MediaType.APPLICATION_JSON_TYPE

    fun render(ex: HttpApplicationException): Response.ResponseBuilder {
        val mediaType = mediaType
        val renderer = renderers.named(mediaType.toString()).firstOrNull()
        if (renderer == null) {
            logger.error("Cannot render exception with type {}: no renderer registered", mediaType)
            return Response.status(ex.status)
        }

        val entity = renderer.render(ex)

        val builder = Response.status(ex.status)
            .entity(entity)
            .header("Content-Type", mediaType.withCharset("utf-8").toString())

        ex.additionalHeaders.forEach { (name, value) ->
            builder.header(name, value)
        }

        return builder
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExceptionRenderers::class.java)
        private val supportedTypes = setOf(MediaType.WILDCARD_TYPE, MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_HTML_TYPE, MediaType.TEXT_PLAIN_TYPE)
    }
}
