/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.managementportal

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Context
import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.auth.AuthValidator

/** Creates a TokenValidator based on the current management portal configuration. */
class ManagementPortalTokenValidator(
    @Context private val tokenValidator: TokenValidator
) : AuthValidator {
    override fun verify(token: String, request: ContainerRequestContext): RadarToken? =
        tokenValidator.validateBlocking(token)
}
