/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.disabled

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.auth.authorization.AuthorizationOracle
import org.radarbase.jersey.auth.AuthValidator
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

/**
 * Registration for authorization against a ManagementPortal. It requires managementPortalUrl and
 * jwtResourceName to be set in the AuthConfig.
 */
class DisabledAuthorizationResourceEnhancer : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {
        bind(DisabledAuthValidator::class.java)
            .to(AuthValidator::class.java)
            .`in`(Singleton::class.java)

        bind(DisabledAuthorizationOracle::class.java)
            .to(AuthorizationOracle::class.java)
            .`in`(Singleton::class.java)
    }
}
