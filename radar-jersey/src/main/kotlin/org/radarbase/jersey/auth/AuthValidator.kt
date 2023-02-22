/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth

import jakarta.ws.rs.container.ContainerRequestContext
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.auth.filter.AuthenticationFilter

interface AuthValidator {
    @Throws(TokenValidationException::class)
    fun verify(token: String, request: ContainerRequestContext): RadarToken?

    fun getToken(request: ContainerRequestContext): String? {
        val authorizationHeader = request.getHeaderString("Authorization")

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader != null
                && authorizationHeader.startsWith(AuthenticationFilter.BEARER, ignoreCase = true)) {
            // Extract the token from the HTTP Authorization header
            return authorizationHeader.substring(AuthenticationFilter.BEARER.length).trim { it <= ' ' }
        }

        // Extract the token from the Authorization cookie
        val authorizationCookie = request.cookies["authorizationBearer"]
        if (authorizationCookie != null) {
            return authorizationCookie.value
        }

        return null
    }
}
