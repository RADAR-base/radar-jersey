/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.filter

import org.slf4j.LoggerFactory
import jakarta.inject.Singleton
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import jakarta.ws.rs.ext.Provider

@Provider
@Singleton
class ResponseLoggerFilter : ContainerResponseFilter {
    override fun filter(requestContext: ContainerRequestContext?, responseContext: ContainerResponseContext?) {
        when {
            requestContext == null || responseContext == null -> return
            requestContext.mediaType == null -> logger.info(
                    "[{}] {} {} -- <{}> ",
                    responseContext.status,
                    requestContext.method, requestContext.uriInfo.path,
                    responseContext.mediaType)
            requestContext.length < 0 -> logger.info(
                    "[{}] {} {} <{}> -- <{}> ",
                    responseContext.status,
                    requestContext.method, requestContext.uriInfo.path, requestContext.mediaType,
                    responseContext.mediaType)
            else -> logger.info(
                    "[{}] {} {} <{}: {}> -- <{}> ",
                    responseContext.status,
                    requestContext.method, requestContext.uriInfo.path, requestContext.mediaType, requestContext.length,
                    responseContext.mediaType)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ResponseLoggerFilter::class.java)
    }
}
