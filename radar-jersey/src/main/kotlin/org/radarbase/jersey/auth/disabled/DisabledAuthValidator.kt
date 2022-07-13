package org.radarbase.jersey.auth.disabled

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Context
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.AuthValidator

/** Authorization validator that grants permission to all resources. */
class DisabledAuthValidator(
        @Context private val config: AuthConfig
) : AuthValidator {
    override fun getToken(request: ContainerRequestContext): String = ""
    override fun verify(token: String, request: ContainerRequestContext): Auth = DisabledAuth(
            config.jwtResourceName)
}
