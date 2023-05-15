/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.filter

import jakarta.annotation.Priority
import jakarta.inject.Singleton
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.ext.Provider
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.jersey.auth.AuthValidator
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.exception.HttpUnauthorizedException
import org.radarbase.jersey.exception.HttpUnauthorizedException.Companion.wwwAuthenticateHeader

/**
 * Authenticates user by a JWT in the bearer signed by the Management Portal.
 */
@Provider
@Authenticated
@Priority(Priorities.AUTHENTICATION)
@Singleton
class AuthenticationFilter(
    @Context private val validator: AuthValidator,
) : ContainerRequestFilter {

    override fun filter(requestContext: ContainerRequestContext) {
        val rawToken = validator.getToken(requestContext)
            ?: throw HttpUnauthorizedException(
                code = "token_missing",
                detailedMessage = "No bearer token is provided in the request.",
                wwwAuthenticateHeader = wwwAuthenticateHeader(),
            )

        val radarToken = try {
            validator.verify(rawToken, requestContext)
        } catch (ex: TokenValidationException) {
            throw HttpUnauthorizedException(
                code = "token_unverified",
                detailedMessage = "Cannot verify token. It may have been rendered invalid.",
                wwwAuthenticateHeader = wwwAuthenticateHeader("invalid_token", ex.message),
            )
        } ?: throw HttpUnauthorizedException(
            code = "token_invalid",
            detailedMessage = "Bearer token is not a valid JWT.",
            wwwAuthenticateHeader = wwwAuthenticateHeader(),
        )

        requestContext.securityContext = RadarSecurityContext(radarToken)
    }

    companion object {
        const val BEARER = "Bearer "
    }
}
