package org.radarbase.jersey.service.managementportal

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.management.client.MPClient
import java.util.function.Supplier
import javax.ws.rs.core.Context

class MPClientFactory(
    @Context private val authConfig: AuthConfig,
    @Context private val okHttpClient: OkHttpClient,
    @Context private val objectMapper: ObjectMapper,
) : Supplier<MPClient> {
    override fun get(): MPClient = MPClient(
        serverConfig = MPClient.MPServerConfig(
            url = requireNotNull(authConfig.managementPortal.httpUrl) { "ManagementPortal client needs a URL" }.toString(),
            clientId = requireNotNull(authConfig.managementPortal.clientId) { "ManagementPortal client needs a client ID" },
            clientSecret = requireNotNull(authConfig.managementPortal.clientSecret) { "ManagementPortal client needs a client secret" },
        ),
        objectMapper = objectMapper,
        httpClient = okHttpClient,
    )
}
