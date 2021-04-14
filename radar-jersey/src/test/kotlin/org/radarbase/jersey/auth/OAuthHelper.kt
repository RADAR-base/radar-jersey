package org.radarbase.jersey.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import okhttp3.Request
import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.config.TokenValidatorConfig
import java.net.URI
import java.security.KeyStore
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.time.Duration
import java.time.Instant
import java.util.*

/**
 * Created by dverbeec on 29/06/2017.
 */
class OAuthHelper {
    val tokenValidator: TokenValidator
    val validEcToken: String

    init {
        val ks = KeyStore.getInstance("PKCS12")
        val resource = checkNotNull(javaClass.getResourceAsStream("/config/keystore.p12")) { "Failed to load key store" }
        resource.use { keyStream ->
            ks.load(keyStream, TEST_KEYSTORE_PASSWORD.toCharArray())
        }

        // get the EC keypair for signing
        val privateKey = ks.getKey(TEST_SIGNKEY_ALIAS,
                TEST_KEYSTORE_PASSWORD.toCharArray()) as ECPrivateKey
        val cert = ks.getCertificate(TEST_SIGNKEY_ALIAS)
        val publicKey = cert.publicKey as ECPublicKey

        val ecdsa = Algorithm.ECDSA256(publicKey, privateKey)
        validEcToken = createValidToken(ecdsa)

        val verifiers = listOf(JWT.require(ecdsa).withIssuer(ISS).build())
        val validatorConfig = object : TokenValidatorConfig {
            override fun getPublicKeyEndpoints(): List<URI> = emptyList()

            override fun getResourceName(): String = ISS

            override fun getPublicKeys(): List<String> = emptyList()
        }
        @Suppress("DEPRECATION")
        tokenValidator = object : TokenValidator(verifiers, validatorConfig) {
            override fun refresh() {
                // do nothing
            }
        }
    }

    private fun createValidToken(algorithm: Algorithm): String {
        val now = Instant.now()
        val exp = now.plus(Duration.ofMinutes(30))
        return JWT.create()
                .withIssuer(ISS)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .withAudience("res_ManagementPortal")
                .withSubject(USER)
                .withArrayClaim("scope", SCOPES)
                .withArrayClaim("authorities", AUTHORITIES)
                .withArrayClaim("roles", ROLES)
                .withArrayClaim("sources", SOURCES)
                .withArrayClaim("aud", AUD)
                .withClaim("client_id", CLIENT)
                .withClaim("user_name", USER)
                .withClaim("jti", JTI)
                .withClaim("grant_type", "password")
                .sign(algorithm)
    }

    companion object {
        private const val TEST_SIGNKEY_ALIAS = "radarbase-managementportal-ec"
        private const val TEST_KEYSTORE_PASSWORD = "radarbase"
        private val SCOPES = Permission.allPermissions()
                .map { it.scopeName() }
                .toTypedArray()

        private val AUTHORITIES = arrayOf("ROLE_SYS_ADMIN")
        private val ROLES = arrayOf<String>()
        private val SOURCES = arrayOf<String>()
        private val AUD = arrayOf("res_ManagementPortal")
        private const val CLIENT = "unit_test"
        private const val USER = "admin"
        private const val ISS = "RADAR"
        private const val JTI = "some-jwt-id"

        fun Request.Builder.bearerHeader(oauth: OAuthHelper) = header(
                "Authorization", "Bearer ${oauth.validEcToken}")
    }
}

