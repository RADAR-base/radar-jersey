/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth.filter

import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.exception.HttpForbiddenException
import org.radarbase.jersey.service.ProjectService
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.ResourceInfo
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.UriInfo
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.exception.HttpNotFoundException

/**
 * Check that the token has given permissions.
 */
class PermissionFilter(
        @Context private val resourceInfo: ResourceInfo,
        @Context private val auth: Auth,
        @Context private val projectService: ProjectService,
        @Context private val uriInfo: UriInfo
) : ContainerRequestFilter {
    override fun filter(requestContext: ContainerRequestContext) {
        val resourceMethod = resourceInfo.resourceMethod
        val annotation = resourceMethod.getAnnotation(NeedsPermission::class.java)
        val permission = annotation.permission
        val location = "${requestContext.method} ${requestContext.uriInfo.path}"

        if (!auth.token.hasPermission(permission)) {
            throw reject(permission)
        }

        val projectId = annotation.projectPathParam.fetchPathParam()
        val userId = annotation.userPathParam.fetchPathParam()
        var organizationId = annotation.organizationPathParam.fetchPathParam()

        val projectIds = when {
            projectId != null -> {
                projectService.ensureProject(projectId)
                val projectOrganization = projectService.projectOrganization(projectId)
                if (organizationId == null) {
                    organizationId = projectOrganization
                } else if (organizationId != projectId) {
                    throw HttpNotFoundException(
                        "organization_not_found",
                        "Organization $organizationId not found for project $projectId."
                    )
                }
                listOf(projectId)
            }
            organizationId != null -> {
                projectService.ensureOrganization(organizationId)
                projectService.listProjects(organizationId)
            }
            else -> emptyList()
        }

        val isAuthorized = when {
            userId != null -> projectIds.any { auth.token.hasPermissionOnSubject(permission, it, userId) }
            organizationId != null -> auth.token.hasPermissionOnOrganization(permission, organizationId) || projectIds.any { auth.token.hasPermissionOnOrganizationAndProject(permission, organizationId, it) }
            else -> true
        }

        auth.logPermission(isAuthorized, permission, location, organizationId, projectIds, userId)

        if (!isAuthorized) {
            val message = "$permission permission not given."
            throw HttpForbiddenException("insufficient_scope", message, additionalHeaders = listOf(
                    "WWW-Authenticate" to (AuthenticationFilter.BEARER_REALM
                    + " error=\"insufficient_scope\""
                    + " error_description=\"$message\""
                    + " scope=\"$permission\"")))
        }
        if (projectId != null) projectService.ensureProject(projectId)
        if (organizationId != null) projectService.ensureOrganization(organizationId)
    }

    private fun reject(permission: Permission): HttpForbiddenException {
        val message = "$permission permission not given."
        return HttpForbiddenException("insufficient_scope", message, additionalHeaders = listOf(
            "WWW-Authenticate" to (AuthenticationFilter.BEARER_REALM
                + " error=\"insufficient_scope\""
                + " error_description=\"$message\""
                + " scope=\"$permission\"")))
    }

    private fun String.fetchPathParam(): String? = if (isNotEmpty()) {
        uriInfo.pathParameters[this]?.firstOrNull()
    } else null
}
