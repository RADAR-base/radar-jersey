/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.managementportal

import com.auth0.jwt.interfaces.DecodedJWT
import com.fasterxml.jackson.databind.JsonNode
import org.radarbase.auth.authorization.Permission.MEASUREMENT_CREATE
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.token.JwtRadarToken
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.auth.Auth

/**
 * Parsed JWT for validating authorization of data contents.
 */
class ManagementPortalAuth(private val jwt: DecodedJWT) : Auth {
    override val token: RadarToken = JwtRadarToken(jwt)
    override val defaultProject: String? = token.getReferentsWithPermission(RoleAuthority.Scope.PROJECT, MEASUREMENT_CREATE)
        .findAny()
        .orElse(null)

    override fun getClaim(name: String): JsonNode = jwt.getClaim(name).`as`(JsonNode::class.java)

    override fun hasRole(projectId: String, role: String): Boolean {
        val authority = RoleAuthority.valueOfAuthorityOrNull(role) ?: return false
        return token.roles.any { it.role == authority && it.referent == projectId }
    }
}
