/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.exception.mapper

import com.fasterxml.jackson.core.JsonProcessingException
import jakarta.inject.Singleton
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.radarbase.jersey.exception.HttpBadRequestException
import org.slf4j.LoggerFactory

/** Handle exceptions without a specific mapper. */
@Provider
@Singleton
class JsonProcessingExceptionMapper(
    @Context private val uriInfo: UriInfo,
    @Context private val requestContext: ContainerRequestContext,
    @Context private val renderers: ExceptionRenderers,
) : ExceptionMapper<JsonProcessingException> {

    override fun toResponse(exception: JsonProcessingException): Response {
        logger.error("[400] {} {} {}", requestContext.method, uriInfo.path, exception.toString())
        return renderers
            .render(HttpBadRequestException("json_processing", "Failed to map JSON: $exception"))
            .build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JsonProcessingExceptionMapper::class.java)
    }
}
