/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.filter

import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.jersey.auth.AuthValidator
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.exception.HttpUnauthorizedException
import jakarta.annotation.Priority
import jakarta.inject.Singleton
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.ext.Provider

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
                ?: throw HttpUnauthorizedException(
                        code = "token_missing",
                        detailedMessage = "No bearer token is provided in the request.",
                        additionalHeaders = listOf("WWW-Authenticate" to BEARER_REALM))

        val radarToken = try {
            validator.verify(rawToken, requestContext)
        } catch (ex: TokenValidationException) {
            throw HttpUnauthorizedException(
                    code = "token_unverified",
                    detailedMessage = "Cannot verify token. It may have been rendered invalid.",
                    additionalHeaders = listOf("WWW-Authenticate" to BEARER_REALM
                            + " error=\"invalid_token\""
                            + " error_description=\"${ex.message}\""))
        } ?: throw HttpUnauthorizedException(
                code = "token_invalid",
                detailedMessage = "Bearer token is not a valid JWT.",
                additionalHeaders = listOf("WWW-Authenticate" to BEARER_REALM))


        requestContext.securityContext = RadarSecurityContext(radarToken)
    }

    companion object {
        const val BEARER_REALM: String = "Bearer realm=\"Kafka REST Proxy\""
        const val BEARER = "Bearer "
    }
}
