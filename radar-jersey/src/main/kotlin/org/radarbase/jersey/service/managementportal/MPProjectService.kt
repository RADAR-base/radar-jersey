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
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.AuthService
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.kotlin.coroutines.CacheConfig
import org.radarbase.kotlin.coroutines.CachedMap
import org.radarbase.management.client.MPClient
import org.radarbase.management.client.MPOrganization
import org.radarbase.management.client.MPProject
import org.radarbase.management.client.MPSubject
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toKotlinDuration

class MPProjectService(
    @Context private val config: AuthConfig,
    @Context private val mpClient: MPClient,
    @Context private val authService: Provider<AuthService>,
) : RadarProjectService {
    private val projects: CachedMap<String, MPProject>
    private val organizations: CachedMap<String, MPOrganization>
    private val participants: ConcurrentMap<String, CachedMap<String, MPSubject>> = ConcurrentHashMap()

    private val projectCacheConfig = CacheConfig(
        refreshDuration = config.managementPortal.syncParticipantsInterval.toKotlinDuration(),
        retryDuration = RETRY_INTERVAL,
    )

    init {
        val cacheConfig = CacheConfig(
            refreshDuration = config.managementPortal.syncProjectsInterval.toKotlinDuration(),
            retryDuration = RETRY_INTERVAL,
        )

        organizations = CachedMap(cacheConfig) {
            mpClient.requestOrganizations()
                .associateBy { it.id }
                .also { logger.debug("Fetched organizations {}", it) }
        }

        projects = CachedMap(cacheConfig) {
            mpClient.requestProjects()
                .associateBy { it.id }
                .also { logger.debug("Fetched projects {}", it) }
        }
    }

    override suspend fun userProjects(permission: Permission): List<MPProject> {
        val authService = authService.get()
        return projects.get()
            .values
            .filter {
                authService.hasPermission(
                    permission,
                    EntityDetails(
                        organization = it.organization?.id,
                        project = it.id,
                    ),
                )
            }
    }

    override suspend fun ensureProject(projectId: String) {
        if (!projects.contains(projectId)) {
            throw HttpNotFoundException("project_not_found", "Project $projectId not found in Management Portal.")
        }
    }

    override suspend fun project(projectId: String): MPProject = projects.get(projectId)
        ?: throw HttpNotFoundException("project_not_found", "Project $projectId not found in Management Portal.")

    override suspend fun projectSubjects(projectId: String): List<MPSubject> = projectUserCache(projectId).get().values.toList()

    override suspend fun subjectByExternalId(projectId: String, externalUserId: String): MPSubject? = projectUserCache(projectId)
        .findValue { it.externalId == externalUserId }

    override suspend fun ensureSubject(projectId: String, userId: String) {
        ensureProject(projectId)
        if (!projectUserCache(projectId).contains(userId)) {
            throw HttpNotFoundException("user_not_found", "User $userId not found in project $projectId of ManagementPortal.")
        }
    }

    override suspend fun ensureOrganization(organizationId: String) {
        if (!organizations.contains(organizationId)) {
            throw HttpNotFoundException("organization_not_found", "Organization $organizationId not found in Management Portal.")
        }
    }

    override suspend fun listProjects(organizationId: String): List<String> = projects.get().asSequence()
        .filter { it.value.organization?.id == organizationId }
        .mapTo(ArrayList()) { it.key }

    override suspend fun projectOrganization(projectId: String): String =
        projects.get(projectId)?.organization?.id
            ?: throw HttpNotFoundException("project_not_found", "Project $projectId not found in Management Portal.")

    override suspend fun subject(projectId: String, userId: String): MPSubject? {
        ensureProject(projectId)
        return projectUserCache(projectId).get(userId)
    }

    private suspend fun projectUserCache(projectId: String) = participants.computeIfAbsent(projectId) {
        CachedMap(projectCacheConfig) {
            mpClient.requestSubjects(projectId)
                .associateBy { checkNotNull(it.id) }
        }
    }

    companion object {
        private val RETRY_INTERVAL = 1.minutes
        private val logger = LoggerFactory.getLogger(MPProjectService::class.java)
    }
}
