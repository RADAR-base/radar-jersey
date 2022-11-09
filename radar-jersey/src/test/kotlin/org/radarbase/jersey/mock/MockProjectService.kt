/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.mock

import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.service.ProjectService

class MockProjectService(
    private val projects: Map<String, List<String>>,
) : ProjectService {
    override fun ensureOrganization(organizationId: String) {
        if (organizationId !in projects) {
            throw HttpNotFoundException("organization_not_found", "Project $organizationId not found.")
        }
    }

    override fun listProjects(organizationId: String): List<String> = projects[organizationId]
        ?: throw HttpNotFoundException("organization_not_found", "Project $organizationId not found.")

    override fun projectOrganization(projectId: String): String = projects.entries
        .firstOrNull { (_, ps) -> projectId in ps }
        ?.key
        ?: throw HttpNotFoundException("project_not_found", "Project $projectId not found.")

    override fun ensureProject(projectId: String) {
        if (projects.values.none { projectId in it }) {
            throw HttpNotFoundException("project_not_found", "Project $projectId not found.")
        }
    }
}
