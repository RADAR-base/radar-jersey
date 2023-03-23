/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.filter

import jakarta.inject.Singleton
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import jakarta.ws.rs.ext.Provider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

@Provider
@Singleton
class ResponseLoggerFilter : ContainerResponseFilter {
    override fun filter(
        requestContext: ContainerRequestContext?,
        responseContext: ContainerResponseContext?,
    ) {
        val path = requestContext?.uriInfo?.path ?: return
        val status = responseContext?.status ?: return
        val logLevel = if (path.isHealthEndpoint && status == 200) Level.DEBUG else Level.INFO

        when {
            requestContext.mediaType == null -> logger.log(
                logLevel,
                "[{}] {} {} -- <{}> ",
                status,
                requestContext.method,
                path,
                responseContext.mediaType,
            )
            requestContext.length < 0 -> logger.log(
                logLevel,
                "[{}] {} {} <{}> -- <{}> ",
                status,
                requestContext.method,
                path,
                requestContext.mediaType,
                responseContext.mediaType,
            )
            else -> logger.log(
                logLevel,
                "[{}] {} {} <{}: {}> -- <{}> ",
                status,
                requestContext.method,
                path,
                requestContext.mediaType,
                requestContext.length,
                responseContext.mediaType,
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ResponseLoggerFilter::class.java)

        /** Whether given path matches a health endpoint. */
        private inline val String.isHealthEndpoint: Boolean
            get() = this == "health" || endsWith("/health")
        private fun Logger.log(level: Level, message: String, vararg arguments: Any?) =
            when (level) {
                Level.ERROR -> error(message, *arguments)
                Level.WARN -> warn(message, *arguments)
                Level.INFO -> info(message, *arguments)
                Level.DEBUG -> debug(message, *arguments)
                Level.TRACE -> trace(message, *arguments)
            }
    }
}
