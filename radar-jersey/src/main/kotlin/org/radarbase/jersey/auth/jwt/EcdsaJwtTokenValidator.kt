/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.jwt

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Context
import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.auth.authorization.AuthorityReference
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.auth.AuthValidator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EcdsaJwtTokenValidator constructor(
    @Context private val tokenValidator: TokenValidator,
) : AuthValidator {
    override fun verify(token: String, request: ContainerRequestContext): RadarToken? {
        val project = request.getHeaderString("RADAR-Project")

        return try {
            val radarToken = tokenValidator.validateBlocking(token)
            return radarToken.copyWithRoles(
                buildSet {
                    addAll(radarToken.roles)
                    add(AuthorityReference(RoleAuthority.PARTICIPANT, project))
                },
            )
        } catch (ex: Throwable) {
            logger.warn("JWT verification exception", ex)
            null
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(EcdsaJwtTokenValidator::class.java)
    }
}
