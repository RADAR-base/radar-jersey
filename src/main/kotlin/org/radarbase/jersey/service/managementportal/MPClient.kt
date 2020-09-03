package org.radarbase.jersey.service.managementportal

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.MPConfig
import org.radarbase.jersey.util.requestJson
import org.slf4j.LoggerFactory
import java.net.MalformedURLException
import java.time.Duration
import java.time.Instant
import javax.ws.rs.core.Context

class MPClient(
        @Context config: MPConfig,
        @Context private val objectMapper: ObjectMapper,
        @Context private val auth: Auth,
        @Context private val httpClient: OkHttpClient,
) {
    private val clientId: String = config.clientId
            ?: throw IllegalArgumentException("Cannot configure managementportal client without client ID")
    private val clientSecret: String = config.clientSecret
            ?: throw IllegalArgumentException("Cannot configure managementportal client without client secret")
    private val baseUrl: HttpUrl = config.url?.toHttpUrlOrNull()
            ?: throw MalformedURLException("Cannot parse base URL ${config.url} as an URL")
    private val projectListReader = objectMapper.readerFor(object : TypeReference<List<MPProject>>() {})
    private val userListReader = objectMapper.readerFor(object : TypeReference<List<MPUser>>() {})
    private val tokenReader = objectMapper.readerFor(RestOauth2AccessToken::class.java)

    private var token: String? = null
    private var expiration: Instant? = null

    private val validToken: String?
        get() {
            val localToken = token ?: return null
            expiration?.takeIf { it > Instant.now() } ?: return null
            return localToken
        }

    private fun ensureToken(): String {
        val localToken = validToken

        return if (localToken != null) {
            localToken
        } else {
            val request = Request.Builder().apply {
                url(baseUrl.resolve("oauth/token")!!)
                post(FormBody.Builder().apply {
                    add("grant_type", "client_credentials")
                    add("client_id", clientId)
                    add("client_secret", clientSecret)
                }.build())
                header("Authorization", Credentials.basic(clientId, clientSecret))
            }.build()

            httpClient.requestJson<RestOauth2AccessToken>(request, tokenReader).let {
                expiration = Instant.now() + Duration.ofSeconds(it.expiresIn.toLong()) - Duration.ofMinutes(5)
                token = it.accessToken
                it.accessToken
            }
        }
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
            @JsonProperty("access_token") var accessToken: String,
            @JsonProperty("refresh_token") var refreshToken: String? = null,
            @JsonProperty("expires_in") var expiresIn: Int = 0,
            @JsonProperty("token_type") var tokenType: String? = null,
            @JsonProperty("user_id") var externalUserId: String? = null)

    companion object {
        private val logger = LoggerFactory.getLogger(MPClient::class.java)
    }
}
