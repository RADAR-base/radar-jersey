/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.service

import org.radarbase.jersey.exception.HttpApplicationException

/**
 * Service to keep track of active projects.
 */
interface ProjectService {
    /**
     * Ensure that given organization ID is valid.
     * @throws HttpApplicationException if the organization ID is not a valid organization ID.
     */
    fun ensureOrganization(organizationId: String)

    /**
     * List all project IDs in an organization
     * @throws HttpApplicationException if the organization ID is not a valid organization ID.
     */
    fun listProjects(organizationId: String): List<String>

    /**
     * Find the organization of a project
     * @throws HttpApplicationException if the organization ID is not a valid organization ID.
     */
    fun projectOrganization(projectId: String): String

    /**
     * Ensure that given project ID is valid.
     * @throws HttpApplicationException if the project ID is not a valid project ID.
     */
    fun ensureProject(projectId: String)
}
