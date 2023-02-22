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
import kotlinx.coroutines.*
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.AuthService
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.util.CacheConfig
import org.radarbase.jersey.util.CachedMap
import org.radarbase.management.client.MPClient
import org.radarbase.management.client.MPOrganization
import org.radarbase.management.client.MPProject
import org.radarbase.management.client.MPSubject
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class MPProjectService(
    @Context private val config: AuthConfig,
    @Context private val mpClient: MPClient,
    @Context private val authService: Provider<AuthService>,
) : RadarProjectService {
    private val projects: CachedMap<String, MPProject>
    private val organizations: CachedMap<String, MPOrganization>
    private val participants: ConcurrentMap<String, CachedMap<String, MPSubject>> = ConcurrentHashMap()

    init {
        val cacheConfig = CacheConfig(
            refreshDuration = config.managementPortal.syncProjectsInterval,
            retryDuration = RETRY_INTERVAL,
        )

        organizations = CachedMap(cacheConfig) {
            runBlocking {
                mpClient.requestOrganizations()
                    .associateBy { it.id }
                    .also { logger.debug("Fetched organizations {}", it) }
            }
        }

        projects = CachedMap(cacheConfig) {
            runBlocking {
                mpClient.requestProjects()
                    .associateBy { it.id }
                    .also { logger.debug("Fetched projects {}", it) }
            }
        }
    }

    override fun userProjects(permission: Permission): List<MPProject> {
        return projects.get().values
                .filter {
                    authService.get().hasPermission(
                        permission,
                        EntityDetails(
                            organization = it.organization?.id,
                            project = it.id
                        )
                    )
                }
    }

    override fun ensureProject(projectId: String) {
        if (projectId !in projects) {
            throw HttpNotFoundException("project_not_found", "Project $projectId not found in Management Portal.")
        }
    }

    override fun project(projectId: String): MPProject = projects[projectId]
        ?: throw HttpNotFoundException("project_not_found", "Project $projectId not found in Management Portal.")

    override fun projectSubjects(projectId: String): List<MPSubject> = projectUserCache(projectId).get().values.toList()

    override fun subjectByExternalId(projectId: String, externalUserId: String): MPSubject? = projectUserCache(projectId)
            .findValue { it.externalId == externalUserId }

    override fun ensureSubject(projectId: String, userId: String) {
        ensureProject(projectId)
        if (!projectUserCache(projectId).contains(userId)) {
            throw HttpNotFoundException("user_not_found", "User $userId not found in project $projectId of ManagementPortal.")
        }
    }

    override fun ensureOrganization(organizationId: String) {
        if (organizationId !in organizations) {
            throw HttpNotFoundException("organization_not_found", "Organization $organizationId not found in Management Portal.")
        }
    }

    override fun listProjects(organizationId: String): List<String> = projects.get().asSequence()
        .filter { it.value.organization?.id == organizationId }
        .mapTo(ArrayList()) { it.key }

    override fun projectOrganization(projectId: String): String =
        projects[projectId]?.organization?.id
            ?: throw HttpNotFoundException("project_not_found", "Project $projectId not found in Management Portal.")

    override fun subject(projectId: String, userId: String): MPSubject? {
        ensureProject(projectId)
        return projectUserCache(projectId)[userId]
    }

    private fun projectUserCache(projectId: String) = participants.computeIfAbsent(projectId) {
        CachedMap(CacheConfig(
                refreshDuration = config.managementPortal.syncParticipantsInterval,
                retryDuration = RETRY_INTERVAL)) {
            runBlocking {
                mpClient.requestSubjects(projectId)
                    .associateBy { checkNotNull(it.id) }
            }
        }
    }

    companion object {
        private val RETRY_INTERVAL = Duration.ofMinutes(1)
        private val logger = LoggerFactory.getLogger(MPProjectService::class.java)
    }
}
