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
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.slf4j.LoggerFactory

/** Handle WebApplicationException. This uses the status code embedded in the exception. */
@Provider
@Singleton
class WebApplicationExceptionMapper(
    @Context private val uriInfo: UriInfo,
) : ExceptionMapper<WebApplicationException> {

    override fun toResponse(exception: WebApplicationException): Response {
        val response = exception.response
        logger.error("[{}] {}: {}", response.status, uriInfo.absolutePath, exception.message)
        return response
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WebApplicationExceptionMapper::class.java)
    }
}
