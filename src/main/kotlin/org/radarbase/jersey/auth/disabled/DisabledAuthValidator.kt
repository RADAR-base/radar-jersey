package org.radarbase.jersey.auth.disabled

import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.AuthValidator
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.Context

/** Authorization validator that grants permission to all resources. */
class DisabledAuthValidator(
        @Context private val config: AuthConfig
) : AuthValidator {
    override fun getToken(request: ContainerRequestContext): String? = ""
    override fun verify(token: String, request: ContainerRequestContext): Auth? = DisabledAuth(
            config.jwtResourceName)
}
