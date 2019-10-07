/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.inject

import com.fasterxml.jackson.core.util.BufferRecyclers
import org.glassfish.hk2.api.IterableProvider
import org.radarbase.jersey.exception.HttpApplicationException
import org.radarbase.jersey.exception.mapper.ExceptionRenderer
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider
import kotlin.text.Charsets.UTF_8

@Provider
@Singleton
class HttpApplicationExceptionMapper(
        @Context private val uriInfo: UriInfo,
        @Context private val requestContext: ContainerRequestContext,
        @Context private val renderers: IterableProvider<ExceptionRenderer>
) : ExceptionMapper<HttpApplicationException> {
    override fun toResponse(exception: HttpApplicationException): Response {
        val mediaType = requestContext.acceptableMediaTypes
                .firstOrNull { type -> type in supportedTypes }
                .takeIf { it != MediaType.WILDCARD_TYPE }
                ?: MediaType.TEXT_PLAIN_TYPE

        logger.error("[{}] {} <{}> - {}: {}", exception.status, uriInfo.absolutePath, mediaType, exception.code, exception.detailedMessage)

        val renderer = renderers.named(mediaType.toString()).firstOrNull()

        if (renderer == null) {
            logger.error("Cannot render exception with type {}: no renderer registered", mediaType)
            return Response.status(exception.status).build()
        }

        val entity = renderer.render(exception)

        val responseBuilder = Response.status(exception.status)
                .entity(entity)
                .header("Content-Type", mediaType.withCharset("utf-8").toString())

        exception.additionalHeaders.forEach { (name, value) ->
            responseBuilder.header(name, value)
        }

        return responseBuilder.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HttpApplicationExceptionMapper::class.java)

        private val supportedTypes = setOf(MediaType.WILDCARD_TYPE, MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_HTML_TYPE, MediaType.TEXT_PLAIN_TYPE)
    }
}
