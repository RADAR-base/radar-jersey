/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.exception.mapper

import jakarta.inject.Singleton
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.radarbase.jersey.exception.HttpApplicationException
import org.slf4j.LoggerFactory

@Provider
@Singleton
class HttpApplicationExceptionMapper(
    @Context private val uriInfo: UriInfo,
    @Context private val requestContext: ContainerRequestContext,
    @Context private val renderers: ExceptionRenderers,
) : ExceptionMapper<HttpApplicationException> {
    override fun toResponse(exception: HttpApplicationException): Response {
        logger.error(
            "[{}] {} {} - {}",
            exception.status,
            requestContext.method,
            uriInfo.path,
            exception.codeMessage,
        )

        return renderers.render(exception).build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HttpApplicationExceptionMapper::class.java)
    }
}
