/*
 *  Copyright 2020 The Hyve
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.radarbase.jersey.service.managementportal

import jakarta.inject.Provider
import jakarta.ws.rs.core.Context
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.service.ProjectService

class ProjectServiceWrapper(
    @Context private val radarProjectService: Provider<RadarProjectService>
) : ProjectService {
    override suspend fun ensureOrganization(organizationId: String) =
        radarProjectService.get().ensureOrganization(organizationId)

    override suspend fun listProjects(organizationId: String): List<String> =
        radarProjectService.get().listProjects(organizationId)

    override suspend fun projectOrganization(projectId: String): String =
        radarProjectService.get().projectOrganization(projectId)

    /**
     * Ensures that [projectId] exists in RADAR project service.
     * @throws HttpNotFoundException if the project does not exist.
     */
    override suspend fun ensureProject(projectId: String) = radarProjectService.get().ensureProject(projectId)

    override suspend fun ensureSubject(projectId: String, userId: String) {
        radarProjectService.get().ensureSubject(projectId, userId)
    }
}
