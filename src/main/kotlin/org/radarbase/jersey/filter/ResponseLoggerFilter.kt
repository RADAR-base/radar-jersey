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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.temporal.ChronoField
import javax.inject.Singleton
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.Provider

@Provider
@Singleton
class ResponseLoggerFilter : ContainerResponseFilter {
    override fun filter(requestContext: ContainerRequestContext?, responseContext: ContainerResponseContext?) {
        if (requestContext == null || responseContext == null) {
            return
        }

        val time = dateTimeFormatter.format(LocalDateTime.now(ZoneOffset.UTC))

        if (requestContext.mediaType == null) {
            logger.info("[{}] {} - {} {} -- <{}> ",
                    time,
                    responseContext.status,
                    requestContext.method, requestContext.uriInfo.path,
                    responseContext.mediaType)
        } else {
            logger.info("[{}] {} - {} {} <{}: {}> -- <{}> ",
                    time,
                    responseContext.status,
                    requestContext.method, requestContext.uriInfo.path, requestContext.mediaType, requestContext.length,
                    responseContext.mediaType)
        }
    }

    companion object {
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        private val logger = LoggerFactory.getLogger(ResponseLoggerFilter::class.java)
    }
}
