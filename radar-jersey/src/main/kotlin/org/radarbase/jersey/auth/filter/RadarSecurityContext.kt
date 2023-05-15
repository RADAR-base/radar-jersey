/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.filter

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.SecurityContext
import org.radarbase.auth.authorization.AuthorityReference
import org.radarbase.auth.token.RadarToken
import java.security.Principal

/**
 * Security context from currently parsed authentication.
 */
class RadarSecurityContext(
    /** Get the parsed authentication.  */
    val token: RadarToken,
) : SecurityContext {

    override fun getUserPrincipal(): Principal {
        return Principal { token.username }
    }

    /**
     * Maps roles in the shape `"project:role"` to a Management Portal role. Global roles
     * take the shape of `":global_role"`. This allows for example a
     * `@RolesAllowed(":SYS_ADMIN")` annotation to resolve correctly.
     * @param role role to be mapped
     * @return `true` if the authentication contains given project/role,
     * `false` otherwise
     */
    override fun isUserInRole(role: String): Boolean {
        val roleParts = role
            .split(":")
            .filter { it.isNotEmpty() }
        val authRef = if (roleParts.isEmpty()) {
            return true
        } else if (roleParts.size == 1) {
            AuthorityReference(
                authority = roleParts[0],
            )
        } else {
            AuthorityReference(
                authority = roleParts[1],
                referent = roleParts[0],
            )
        }
        return authRef in token.roles
    }

    override fun isSecure(): Boolean {
        return true
    }

    override fun getAuthenticationScheme(): String {
        return "JWT"
    }

    companion object {
        val ContainerRequestContext.radarSecurityContext: RadarSecurityContext
            get() = checkNotNull(securityContext as? RadarSecurityContext) {
                "RequestContext does not have a RadarSecurityContext"
            }
    }
}
