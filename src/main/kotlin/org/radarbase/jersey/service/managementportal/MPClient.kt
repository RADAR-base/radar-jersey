package org.radarbase.jersey.service.managementportal

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.util.requestJson
import org.slf4j.LoggerFactory
import java.net.MalformedURLException
import java.time.Duration
import java.time.Instant
import javax.ws.rs.core.Context

class MPClient(
        @Context config: AuthConfig,
        @Context private val objectMapper: ObjectMapper,
        @Context private val auth: Auth,
        @Context private val httpClient: OkHttpClient,
) {
    private val clientId: String = config.managementPortal.clientId
            ?: throw IllegalArgumentException("Cannot configure managementportal client without client ID")
    private val clientSecret: String = config.managementPortal.clientSecret
            ?: throw IllegalArgumentException("Cannot configure managementportal client without client secret")
    private val baseUrl: HttpUrl = config.managementPortal.url?.toHttpUrlOrNull()
            ?: throw MalformedURLException("Cannot parse base URL ${config.managementPortal.url} as an URL")

    private val projectListReader = objectMapper.readerFor(object : TypeReference<List<MPProject>>() {})
    private val userListReader = objectMapper.readerFor(object : TypeReference<List<MPUser>>() {})
    private val tokenReader = objectMapper.readerFor(RestOauth2AccessToken::class.java)

    @Volatile
    private var token: RestOauth2AccessToken? = null

    private val validToken: RestOauth2AccessToken?
        get() = token?.takeIf { it.isValid() }

    private fun ensureToken(): String = (validToken
            ?: requestToken().also { token = it })
            .accessToken

    private fun requestToken(): RestOauth2AccessToken {
        val request = Request.Builder().apply {
            url(baseUrl.resolve("oauth/token")!!)
            post(FormBody.Builder().apply {
                add("grant_type", "client_credentials")
                add("client_id", clientId)
                add("client_secret", clientSecret)
            }.build())
            header("Authorization", Credentials.basic(clientId, clientSecret))
        }.build()

        return httpClient.requestJson(request, tokenReader)
    }

    fun readProjects(): List<MPProject> {
        logger.debug("Requesting for projects")
        val request = Request.Builder().apply {
            url(baseUrl.resolve("api/projects")!!)
            header("Authorization", "Bearer ${ensureToken()}")
        }.build()

        return httpClient.requestJson(request, projectListReader)
    }

    fun readParticipants(projectId: String): List<MPUser> {
        val request = Request.Builder().apply {
            url(baseUrl.newBuilder()
                    .addPathSegments("api/projects/$projectId/subjects")
                    .addQueryParameter("page", "0")
                    .addQueryParameter("size", Int.MAX_VALUE.toString())
                    .build())
            header("Authorization", "Bearer ${ensureToken()}")
        }.build()

        return httpClient.requestJson<List<MPUser>>(request, userListReader)
                .map { it.copy(projectId = projectId) }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RestOauth2AccessToken(
            @JsonProperty("access_token") val accessToken: String,
            @JsonProperty("refresh_token") val refreshToken: String? = null,
            @JsonProperty("expires_in") val expiresIn: Long = 0,
            @JsonProperty("token_type") val tokenType: String? = null,
            @JsonProperty("user_id") val externalUserId: String? = null) {
        private val expiration: Instant = Instant.now() + Duration.ofSeconds(expiresIn)  - Duration.ofMinutes(5)
        fun isValid() = Instant.now() < expiration
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MPClient::class.java)
    }
}
