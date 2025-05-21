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
import org.radarbase.jersey.exception.entity.ErrorResponse
import org.slf4j.LoggerFactory

/** Handle exceptions without a specific mapper. */
@Provider
@Singleton
class UnhandledExceptionMapper(
    @Context private val uriInfo: UriInfo,
    @Context private val requestContext: ContainerRequestContext,
) : ExceptionMapper<Throwable> {

    override fun toResponse(exception: Throwable): Response {
        logger.error("[500] {} {} → {}", requestContext.method, uriInfo.path, exception.message, exception)

        val errorResponse = ErrorResponse(
            error = "internal_server_error",
            description = exception.message ?: "An unexpected error occurred.",
        )

        return Response.serverError()
            .header("Content-Type", "application/json; charset=utf-8")
            .entity(errorResponse)
            .build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UnhandledExceptionMapper::class.java)
    }
}
