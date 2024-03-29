/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth

import com.fasterxml.jackson.annotation.JsonIgnore
import org.radarbase.jersey.config.ConfigLoader.copyEnv
import org.radarbase.jersey.config.ConfigLoader.copyOnChange
import java.time.Duration

data class AuthConfig(
    /** ManagementPortal configuration. */
    val managementPortal: MPConfig = MPConfig(),
    /** OAuth 2.0 resource name. */
    val jwtResourceName: String,
    /** OAuth 2.0 issuer. */
    val jwtIssuer: String? = null,
    /** ECDSA public keys used for verifying incoming OAuth 2.0 JWT. */
    val jwtECPublicKeys: List<String>? = null,
    /** RSA public keys used for verifying incoming OAuth 2.0 JWT. */
    val jwtRSAPublicKeys: List<String>? = null,
    /** p12 keystore file path used for verifying incoming OAuth 2.0 JWT. */
    val jwtKeystorePath: String? = null,
    /** Key alias in p12 keystore for verifying incoming OAuth 2.0 JWT. */
    val jwtKeystoreAlias: String? = null,
    /** Key password for the key alias in the p12 keystore. */
    val jwtKeystorePassword: String? = null,
    val jwksUrls: List<String> = emptyList(),
) {
    fun withEnv(): AuthConfig = this
        .copyOnChange(managementPortal, { it.withEnv() }) { copy(managementPortal = it) }
        .copyEnv("AUTH_KEYSTORE_PASSWORD") { copy(jwtKeystorePassword = it) }
}

data class MPConfig(
    /** URL for the current service to find the ManagementPortal installation. */
    val url: String? = null,
    /** OAuth 2.0 client ID to get data from the ManagementPortal with. */
    val clientId: String? = null,
    /** OAuth 2.0 client secret to get data from the ManagementPortal with. */
    val clientSecret: String? = null,
    /** Interval after which the list of projects should be refreshed (minutes). */
    val syncProjectsIntervalMin: Long = 5,
    /** Interval after which the list of subjects in a project should be refreshed (minutes). */
    val syncParticipantsIntervalMin: Long = 5,
) {
    /** Interval after which the list of projects should be refreshed. */
    @JsonIgnore
    val syncProjectsInterval: Duration = Duration.ofMinutes(syncProjectsIntervalMin)

    /** Interval after which the list of subjects in a project should be refreshed. */
    @JsonIgnore
    val syncParticipantsInterval: Duration = Duration.ofMinutes(syncParticipantsIntervalMin)

    fun withEnv(): MPConfig = this
        .copyEnv("MANAGEMENT_PORTAL_CLIENT_ID") { copy(clientId = it) }
        .copyEnv("MANAGEMENT_PORTAL_CLIENT_SECRET") { copy(clientSecret = it) }
}
