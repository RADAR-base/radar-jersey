/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.managementportal

import jakarta.ws.rs.core.Context
import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.auth.config.TokenVerifierPublicKeyConfig
import org.radarbase.jersey.auth.AuthConfig
import java.net.URI
import java.util.function.Supplier

class TokenValidatorFactory(
        @Context private val config: AuthConfig
) : Supplier<TokenValidator> {
    override fun get(): TokenValidator = try {
        TokenValidator()
    } catch (e: RuntimeException) {
        val cfg = TokenVerifierPublicKeyConfig().apply {
            publicKeyEndpoints = listOf(URI("${config.managementPortal.url}/oauth/token_key"))
            resourceName = config.jwtResourceName
        }
        TokenValidator(cfg)
    }
}
