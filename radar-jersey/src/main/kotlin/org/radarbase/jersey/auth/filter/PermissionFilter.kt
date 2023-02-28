/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.filter

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.ResourceInfo
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.UriInfo
import org.radarbase.jersey.auth.AuthService
import org.radarbase.jersey.auth.NeedsPermission

/**
 * Check that the token has given permissions.
 */
class PermissionFilter(
    @Context private val resourceInfo: ResourceInfo,
    @Context private val uriInfo: UriInfo,
    @Context private val authService: AuthService,
) : ContainerRequestFilter {
    override fun filter(requestContext: ContainerRequestContext) {
        val resourceMethod = resourceInfo.resourceMethod
        val annotation = resourceMethod.getAnnotation(NeedsPermission::class.java)

        authService.checkScopeAndPermission(
            permission = annotation.permission,
            location = "${requestContext.method} ${requestContext.uriInfo.path}",
        ) {
            organization = annotation.organizationPathParam.fetchPathParam()
            project = annotation.projectPathParam.fetchPathParam()
            subject = annotation.userPathParam.fetchPathParam()
        }
    }

    private fun String.fetchPathParam(): String? = if (isNotEmpty()) {
        uriInfo.pathParameters[this]?.firstOrNull()
    } else null
}
