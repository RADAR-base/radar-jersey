/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.managementportal

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.auth.authorization.AuthorizationOracle
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.AuthValidator
import org.radarbase.jersey.auth.jwt.AuthorizationOracleFactory
import org.radarbase.jersey.auth.jwt.TokenValidatorFactory
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.service.ProjectService
import org.radarbase.jersey.service.managementportal.MPClientFactory
import org.radarbase.jersey.service.managementportal.MPProjectService
import org.radarbase.jersey.service.managementportal.ProjectServiceWrapper
import org.radarbase.jersey.service.managementportal.RadarProjectService
import org.radarbase.management.client.MPClient

/**
 * Registration for authorization against a ManagementPortal. It requires managementPortalUrl and
 * jwtResourceName to be set in the AuthConfig.
 */
class ManagementPortalResourceEnhancer(private val config: AuthConfig) : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {
        val config = config.withEnv()
        bindFactory(TokenValidatorFactory::class.java)
            .to(TokenValidator::class.java)
            .`in`(Singleton::class.java)

        bind(ManagementPortalTokenValidator::class.java)
            .to(AuthValidator::class.java)
            .`in`(Singleton::class.java)

        bindFactory(AuthorizationOracleFactory::class.java)
            .to(AuthorizationOracle::class.java)
            .`in`(Singleton::class.java)

        if (config.managementPortal.clientId != null) {
            bindFactory(MPClientFactory::class.java)
                .to(MPClient::class.java)
                .`in`(Singleton::class.java)

            bind(ProjectServiceWrapper::class.java)
                .to(ProjectService::class.java)
                .`in`(Singleton::class.java)

            bind(MPProjectService::class.java)
                .to(RadarProjectService::class.java)
                .`in`(Singleton::class.java)
        }
    }
}
