/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.jwt

import jakarta.ws.rs.core.Context
import org.radarbase.auth.authentication.StaticTokenVerifierLoader
import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.auth.authentication.TokenVerifierLoader
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.jwks.*
import org.radarbase.auth.jwks.JwksTokenVerifierLoader.Companion.toTokenVerifier
import org.radarbase.jersey.auth.AuthConfig
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import java.security.KeyStore
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.function.Supplier
import kotlin.io.path.inputStream

class TokenValidatorFactory(
    @Context private val config: AuthConfig,
) : Supplier<TokenValidator> {
    override fun get(): TokenValidator {
        val tokenVerifierLoaders = buildList {
            if (config.managementPortal.url != null) {
                add(config.managementPortal.url + "/oauth/token_key")
            }
            addAll(config.jwksUrls)
        }.mapTo(ArrayList<TokenVerifierLoader>()) {
            JwksTokenVerifierLoader(it, config.jwtResourceName, JwkAlgorithmParser())
        }

        val algorithms = buildList {
            if (!config.jwtECPublicKeys.isNullOrEmpty()) {
                val parser = ECPEMCertificateParser()
                config.jwtECPublicKeys.mapTo(this) { key ->
                    parser.parseAlgorithm(key)
                }
            }
            if (!config.jwtRSAPublicKeys.isNullOrEmpty()) {
                val parser = RSAPEMCertificateParser()
                config.jwtRSAPublicKeys.mapTo(this) { key ->
                    parser.parseAlgorithm(key)
                }
            }

            if (!config.jwtKeystorePath.isNullOrEmpty()) {
                add(
                    try {
                        val pkcs12Store = KeyStore.getInstance("pkcs12")
                        pkcs12Store.load(
                            Paths.get(config.jwtKeystorePath).inputStream(),
                            config.jwtKeystorePassword?.toCharArray(),
                        )
                        when (val publicKey = pkcs12Store.getCertificate(config.jwtKeystoreAlias).publicKey) {
                            is ECPublicKey -> publicKey.toAlgorithm()
                            is RSAPublicKey -> publicKey.toAlgorithm()
                            else -> throw IllegalStateException("Unknown JWT key type ${publicKey.algorithm}")
                        }
                    } catch (ex: Exception) {
                        throw IllegalStateException("Failed to initialize JWT ECDSA public key", ex)
                    },
                )
            }
        }

        if (algorithms.isNotEmpty()) {
            tokenVerifierLoaders += StaticTokenVerifierLoader(
                algorithms.map { algorithm ->
                    algorithm.toTokenVerifier(config.jwtResourceName) {
                        config.jwtIssuer?.let {
                            withIssuer(it)
                        }
                    }
                },
            )
        }

        if (tokenVerifierLoaders.isEmpty()) {
            throw TokenValidationException("No verification algorithms given")
        }

        logger.info("Verifying JWTs with ${tokenVerifierLoaders.size} token verifiers")

        return TokenValidator(
            verifierLoaders = tokenVerifierLoaders,
            fetchTimeout = Duration.ofMinutes(5),
            maxAge = Duration.ofHours(3),
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TokenValidatorFactory::class.java)
    }
}
