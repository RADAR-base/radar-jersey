/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth

data class AuthConfig(
        val managementportal: MPConfig = MPConfig(),
        val jwtResourceName: String,
        val jwtIssuer: String? = null,
        val jwtECPublicKeys: List<String>? = null,
        val jwtRSAPublicKeys: List<String>? = null,
        val jwtKeystorePath: String? = null,
        val jwtKeystorePassword: String? = null,
        val jwtKeystoreAlias: String? = null,
        val clientId: String? = null,
        val clientSecret: String? = null,
        )

data class MPConfig(
        val url: String? = null,
        val clientId: String? = null,
        val clientSecret: String? = null,
        val syncProjectsIntervalMin: Long = 5,
        val syncParticipantsIntervalMin: Long = 5,
)
