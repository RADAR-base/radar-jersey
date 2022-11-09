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
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.filter.RadarSecurityContext
import java.util.function.Supplier

/** Generates radar tokens from the security context. */
class AuthFactory(
        @Context private val context: ContainerRequestContext
) : Supplier<Auth> {
    override fun get() = (context.securityContext as? RadarSecurityContext)?.auth
                ?: throw IllegalStateException("Created null wrapper")
}
