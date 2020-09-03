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
import org.radarbase.jersey.service.ProjectService
import org.radarcns.auth.authorization.Permission
import org.radarcns.auth.authorization.Permission.PROJECT_READ


interface RadarProjectService : ProjectService {
    fun project(projectId: String): MPProject
    fun userProjects(auth: Auth, permission: Permission = PROJECT_READ): List<MPProject>
    fun projectUsers(projectId: String): List<MPUser>
    fun userByExternalId(projectId: String, externalUserId: String): MPUser?
}
