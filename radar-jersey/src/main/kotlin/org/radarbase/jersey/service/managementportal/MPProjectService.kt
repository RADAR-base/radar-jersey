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

import jakarta.ws.rs.core.Context
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.util.CacheConfig
import org.radarbase.jersey.util.CachedMap
import org.radarbase.management.client.MPClient
import org.radarbase.management.client.MPProject
import org.radarbase.management.client.MPSubject
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class MPProjectService(
    @Context private val config: AuthConfig,
    @Context private val mpClient: MPClient,
) : RadarProjectService {
    private val projects = CachedMap(CacheConfig(
            refreshDuration = config.managementPortal.syncProjectsInterval,
            retryDuration = RETRY_INTERVAL)) {
        mpClient.requestProjects()
                .map { it.id to it }
                .toMap()
    }

    private val participants: ConcurrentMap<String, CachedMap<String, MPSubject>> = ConcurrentHashMap()

    override fun userProjects(auth: Auth, permission: Permission): List<MPProject> {
        return projects.get()
                .values
                .filter { auth.token.hasPermissionOnProject(permission, it.id) }
    }

    override fun ensureProject(projectId: String) {
        if (projectId !in projects) {
            throw HttpNotFoundException("project_not_found", "Project $projectId not found in Management Portal.")
        }
    }

    override fun project(projectId: String): MPProject = projects[projectId]
        ?: throw HttpNotFoundException("project_not_found", "Project $projectId not found in Management Portal.")

    override fun projectUsers(projectId: String): List<MPSubject> = projectUserCache(projectId).get().values.toList()

    override fun userByExternalId(projectId: String, externalUserId: String): MPSubject? = projectUserCache(projectId)
            .findValue { it.externalId == externalUserId }

    override fun ensureUser(projectId: String, userId: String) {
        ensureProject(projectId)
        if (!projectUserCache(projectId).contains(userId)) {
            throw HttpNotFoundException("user_not_found", "User $userId not found in project $projectId of ManagementPortal.")
        }
    }

    override fun getUser(projectId: String, userId: String): MPSubject? {
        ensureProject(projectId)
        return projectUserCache(projectId)[userId]
    }

    private fun projectUserCache(projectId: String) = participants.computeIfAbsent(projectId) {
        CachedMap(CacheConfig(
                refreshDuration = config.managementPortal.syncParticipantsInterval,
                retryDuration = RETRY_INTERVAL)) {
            mpClient.requestSubjects(projectId)
                    .mapNotNull { subject -> subject.id?.let { it to subject } }
                    .toMap()
        }
    }

    companion object {
        private val RETRY_INTERVAL = Duration.ofMinutes(1)
    }
}
