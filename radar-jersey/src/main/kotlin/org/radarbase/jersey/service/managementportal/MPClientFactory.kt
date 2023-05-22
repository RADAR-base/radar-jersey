package org.radarbase.jersey.service.managementportal

import jakarta.ws.rs.core.Context
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.ktor.auth.ClientCredentialsConfig
import org.radarbase.ktor.auth.clientCredentials
import org.radarbase.management.client.MPClient
import org.radarbase.management.client.mpClient
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.function.Supplier

class MPClientFactory(
    @Context private val authConfig: AuthConfig,
) : Supplier<MPClient> {
    override fun get(): MPClient = mpClient {
        url = requireNotNull(authConfig.managementPortal.url) { "ManagementPortal client needs a URL" }
            .trimEnd('/')
        auth {
            val authConfig = ClientCredentialsConfig(
                tokenUrl = "$url/oauth/token",
                clientId = authConfig.managementPortal.clientId,
                clientSecret = authConfig.managementPortal.clientSecret,
            ).copyWithEnv()

            logger.info(
                "Configuring MPClient with URL {} and client ID {}",
                authConfig.tokenUrl,
                authConfig.clientId
            )

            clientCredentials(
                authConfig = authConfig,
                targetHost = URI.create(url!!).host,
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MPClientFactory::class.java)
    }
}
