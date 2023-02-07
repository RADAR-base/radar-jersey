package org.radarbase.jersey.service.managementportal

import jakarta.ws.rs.core.Context
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.management.auth.ClientCredentialsConfig
import org.radarbase.management.auth.clientCredentials
import org.radarbase.management.client.MPClient
import org.radarbase.management.client.mpClient
import java.util.function.Supplier

class MPClientFactory(
    @Context private val authConfig: AuthConfig,
) : Supplier<MPClient> {
    override fun get(): MPClient = mpClient {
        val mpUrl = requireNotNull(authConfig.managementPortal.url) { "ManagementPortal client needs a URL" }
            .trimEnd('/') + '/'
        auth {
            clientCredentials(
                ClientCredentialsConfig(
                    tokenUrl = "$mpUrl/oauth/token",
                    clientId = authConfig.managementPortal.clientId,
                    clientSecret = authConfig.managementPortal.clientSecret,
                ).copyWithEnv()
            )
        }
        url = mpUrl
    }
}
