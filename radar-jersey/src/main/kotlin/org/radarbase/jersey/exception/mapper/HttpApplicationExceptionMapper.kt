/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.exception.mapper

import org.glassfish.hk2.api.IterableProvider
import org.radarbase.jersey.exception.HttpApplicationException
import org.slf4j.LoggerFactory
import jakarta.inject.Singleton
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

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
                ?: MediaType.APPLICATION_JSON_TYPE

        logger.error("[{}] {} {} - {}: {}", exception.status, requestContext.method, uriInfo.path, exception.code, exception.detailedMessage)

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
