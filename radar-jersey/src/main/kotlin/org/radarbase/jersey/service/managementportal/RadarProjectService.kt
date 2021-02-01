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

import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.service.ProjectService
import org.radarbase.management.client.MPProject
import org.radarbase.management.client.MPSubject
import org.radarcns.auth.authorization.Permission
import org.radarcns.auth.authorization.Permission.PROJECT_READ


interface RadarProjectService : ProjectService {
    override fun ensureProject(projectId: String) {
        project(projectId)
    }

    /**
     * Ensures that [projectId] exists in ManagementPortal.
     * @throws HttpNotFoundException if the project does not exist.
     */
    fun project(projectId: String): MPProject

    /**
     * Returns all ManagementPortal projects that the current user has access to.
     */
    fun userProjects(auth: Auth, permission: Permission = PROJECT_READ): List<MPProject>

    /**
     * Get project with [projectId] in ManagementPortal.
     * @throws HttpNotFoundException if the project does not exist.
     */
    fun projectUsers(projectId: String): List<MPSubject>

    /**
     * Get subject with [externalUserId] from [projectId] in ManagementPortal.
     * @throws HttpNotFoundException if the project does not exist.
     */
    fun userByExternalId(projectId: String, externalUserId: String): MPSubject?

    fun getUser(projectId: String, userId: String): MPSubject?
    fun ensureUser(projectId: String, userId: String)
}
