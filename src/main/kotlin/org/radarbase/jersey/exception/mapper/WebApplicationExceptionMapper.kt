/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.exception.mapper

import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/** Handle WebApplicationException. This uses the status code embedded in the exception. */
@Provider
@Singleton
class WebApplicationExceptionMapper(
        @Context private val uriInfo: UriInfo
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
