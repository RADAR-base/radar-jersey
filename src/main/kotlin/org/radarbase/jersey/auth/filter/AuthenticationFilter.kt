/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.filter

import org.radarbase.jersey.auth.AuthValidator
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.exception.HttpUnauthorizedException
import org.radarcns.auth.exception.TokenValidationException
import org.slf4j.LoggerFactory
import javax.annotation.Priority
import javax.inject.Singleton
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.core.Context
import javax.ws.rs.ext.Provider

/**
 * Authenticates user by a JWT in the bearer signed by the Management Portal.
 */
@Provider
@Authenticated
@Priority(Priorities.AUTHENTICATION)
@Singleton
class AuthenticationFilter(
        @Context private val validator: AuthValidator
) : ContainerRequestFilter {

    override fun filter(requestContext: ContainerRequestContext) {
        val rawToken = validator.getToken(requestContext)

        if (rawToken == null) {
            logger.warn("[401] {}: No token bearer header provided in the request",
                    requestContext.uriInfo.path)
            throw HttpUnauthorizedException(
                    code = "token_missing",
                    detailedMessage = "No bearer token is provided in the request.",
                    additionalHeaders = listOf("WWW-Authenticate" to BEARER_REALM))
        }

        val radarToken = try {
            validator.verify(rawToken, requestContext)
        } catch (ex: TokenValidationException) {
            logger.warn("[401] {}: {}", requestContext.uriInfo.path, ex.toString())
            throw HttpUnauthorizedException(
                    code = "token_unverified",
                    detailedMessage = "Cannot verify token. It may have been rendered invalid.",
                    additionalHeaders = listOf("WWW-Authenticate" to BEARER_REALM
                            + " error=\"invalid_token\""
                            + " error_description=\"${ex.message}\""))
        }

        if (radarToken == null) {
            logger.warn("[401] {}: Bearer token invalid",
                    requestContext.uriInfo.path)
            throw HttpUnauthorizedException(
                    code = "token_invalid",
                    detailedMessage = "Bearer token is not a valid JWT.",
                    additionalHeaders = listOf("WWW-Authenticate" to BEARER_REALM))
        } else {
            requestContext.securityContext = RadarSecurityContext(radarToken)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthenticationFilter::class.java)

        const val BEARER_REALM: String = "Bearer realm=\"Kafka REST Proxy\""
        const val BEARER = "Bearer "
    }
}
