/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.jwt

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.auth.authorization.AuthorizationOracle
import org.radarbase.jersey.auth.AuthValidator
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

/**
 * Registration for authorization against a generic OAuth 2.0 provider.
 *
 * It requires jwtResourceName to be set in the AuthConfig. It also needs to have a signature
 * validation set, e.g. jwtECPublicKeys, jwtRSAPublicKeys or jwtKeystorePath with jwtKeystorePassword
 * and jwtKeystoreAlias. If jwtIssuer is set, the issuer of the JWT will also be validated.
 */
class EcdsaResourceEnhancer : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {
        bindFactory(TokenValidatorFactory::class.java)
            .to(TokenValidator::class.java)
            .`in`(Singleton::class.java)

        bind(EcdsaJwtTokenValidator::class.java)
            .to(AuthValidator::class.java)
            .`in`(Singleton::class.java)

        bindFactory(AuthorizationOracleFactory::class.java)
            .to(AuthorizationOracle::class.java)
            .`in`(Singleton::class.java)
    }
}
