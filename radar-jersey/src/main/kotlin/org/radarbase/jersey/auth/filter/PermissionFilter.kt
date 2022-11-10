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
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.service.ProjectService

/**
 * Check that the token has given permissions.
 */
class PermissionFilter(
    @Context private val resourceInfo: ResourceInfo,
    @Context private val auth: Auth,
    @Context private val projectService: ProjectService,
    @Context private val uriInfo: UriInfo,
) : ContainerRequestFilter {
    override fun filter(requestContext: ContainerRequestContext) {
        val resourceMethod = resourceInfo.resourceMethod
        val annotation = resourceMethod.getAnnotation(NeedsPermission::class.java)
        val permission = annotation.permission
        val location = "${requestContext.method} ${requestContext.uriInfo.path}"

        if (!auth.token.hasPermission(permission)) {
            throw auth.forbiddenException(permission, location)
        }

        val projectId = annotation.projectPathParam.fetchPathParam()
        val userId = annotation.userPathParam.fetchPathParam()
        val organizationId = annotation.organizationPathParam.fetchPathParam()

        val hierarchy = when {
            projectId != null -> hierarchyByProject(organizationId, projectId)
            userId != null -> throw HttpNotFoundException(
                "user_not_found",
                "User $userId not found without project ID"
            )
            organizationId != null -> hierarchyByOrganization(organizationId)
            else -> null
        }

        if (
            hierarchy == null ||
            auth.token.hasPermissionOnOrganization(permission, hierarchy.organizationId)
        ) {
            // no more detailed authorization is needed or organization permissions are sufficient
        } else if (userId != null) {
            checkNotNull(projectId) { "Ensured by above hierarchy check." }
            if (!auth.token.hasPermissionOnSubject(permission, projectId, userId)) {
                throw auth.forbiddenException(
                    permission,
                    location,
                    hierarchy.organizationId,
                    hierarchy.projectIds,
                    userId
                )
            }
            projectService.ensureSubject(projectId, userId)
        } else if (!auth.token.hasPermissionOnProjects(permission, hierarchy)) {
            throw auth.forbiddenException(
                permission,
                location,
                hierarchy.organizationId,
                hierarchy.projectIds
            )
        }

        auth.logAuthorized(permission, location, hierarchy?.organizationId, hierarchy?.projectIds, userId)
    }

    private fun hierarchyByProject(organizationId: String?, projectId: String): ProjectHierarchy {
        projectService.ensureProject(projectId)
        val projectOrganization = projectService.projectOrganization(projectId)
        if (organizationId != null && organizationId != projectOrganization) {
            throw HttpNotFoundException(
                "organization_not_found",
                "Organization $organizationId not found for project $projectId."
            )
        }
        return ProjectHierarchy(projectOrganization, listOf(projectId))
    }

    private fun hierarchyByOrganization(organizationId: String): ProjectHierarchy {
        projectService.ensureOrganization(organizationId)
        return ProjectHierarchy(organizationId, projectService.listProjects(organizationId))
    }

    private fun String.fetchPathParam(): String? = if (isNotEmpty()) {
        uriInfo.pathParameters[this]?.firstOrNull()
    } else null

    private data class ProjectHierarchy(
        val organizationId: String,
        val projectIds: List<String>,
    )

    companion object {
        private fun RadarToken.hasPermissionOnProjects(
            permission: Permission,
            hierarchy: ProjectHierarchy,
        ) = hierarchy.projectIds.any {
            hasPermissionOnProject(
                permission,
                it
            )
        }
    }
}
