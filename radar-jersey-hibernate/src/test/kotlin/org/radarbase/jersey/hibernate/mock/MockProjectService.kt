/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.hibernate.mock

import jakarta.ws.rs.core.Context
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.hibernate.db.ProjectRepository
import org.radarbase.jersey.service.ProjectService

class MockProjectService(
    @Context private val projects: ProjectRepository,
) : ProjectService {
    override suspend fun ensureOrganization(organizationId: String) {
        if (projects.list().none { it.organization == organizationId }) {
            throw HttpNotFoundException("organization_not_found", "Organization $organizationId not found.")
        }
    }

    override suspend fun listProjects(organizationId: String): List<String> = projects.list()
        .filter { it.organization == organizationId }
        .map { it.name }

    override suspend fun projectOrganization(projectId: String): String = projects.list()
        .firstOrNull { it.name == projectId }
        ?.organization
        ?: throw HttpNotFoundException("project_not_found", "Project $projectId not found.")

    override suspend fun ensureProject(projectId: String) {
        if (projects.list().none { it.name == projectId }) {
            throw HttpNotFoundException("project_not_found", "Project $projectId not found.")
        }
    }

    override suspend fun ensureSubject(projectId: String, userId: String) {
        ensureProject(projectId)
    }
}
