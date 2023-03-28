/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.jwt

import jakarta.ws.rs.core.Context
import org.glassfish.jersey.server.ContainerRequest
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.auth.filter.RadarSecurityContext.Companion.radarSecurityContext
import java.util.function.Supplier

/** Generates radar tokens from the security context. */
class RadarTokenFactory(
    @Context private val context: ContainerRequest,
) : Supplier<RadarToken> {
    override fun get(): RadarToken = context.radarSecurityContext.token
}
