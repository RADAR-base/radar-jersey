package org.radarbase.jersey.auth.disabled

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Context
import org.radarbase.auth.token.DataRadarToken
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.AuthValidator
import java.time.Instant

/** Authorization validator that grants permission to all resources. */
class DisabledAuthValidator(
        @Context private val config: AuthConfig
) : AuthValidator {
    override fun getToken(request: ContainerRequestContext): String = ""
    override fun verify(token: String, request: ContainerRequestContext): RadarToken = DataRadarToken(
        audience = listOf(config.jwtResourceName),
        expiresAt = Instant.MAX,
        roles = setOf(),
        scopes = setOf(),
        grantType = "disabled",
        username = "anonymous",
    )
}
