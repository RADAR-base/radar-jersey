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
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.util.CachedSet
import org.radarcns.auth.authorization.Permission
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.ws.rs.core.Context

class MPProjectService(
        @Context private val config: AuthConfig,
        @Context private val mpClient: MPClient,
) : RadarProjectService {
    private val projects = CachedSet(config.managementPortal.syncProjectsInterval, RETRY_INTERVAL) {
        mpClient.readProjects()
    }

    private val participants: ConcurrentMap<String, CachedSet<MPUser>> = ConcurrentHashMap()

    override fun userProjects(auth: Auth, permission: Permission): List<MPProject> {
        return projects.get()
            .filter { auth.token.hasPermissionOnProject(permission, it.id) }
    }

    override fun project(projectId: String): MPProject = projects.find { it.id == projectId }
        ?: throw HttpNotFoundException("project_not_found", "Project $projectId not found in Management Portal.")

    override fun projectUsers(projectId: String): List<MPUser> {
        ensureProject(projectId)
        val projectParticipants = participants.computeIfAbsent(projectId) {
            CachedSet(config.managementPortal.syncParticipantsInterval, RETRY_INTERVAL) {
                mpClient.readParticipants(projectId)
            }
        }

        return projectParticipants.get().toList()
    }

    companion object {
        private val RETRY_INTERVAL = Duration.ofMinutes(1)
    }
}
