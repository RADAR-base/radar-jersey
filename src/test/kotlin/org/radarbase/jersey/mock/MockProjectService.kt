/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.mock

import org.radarbase.jersey.auth.ProjectService
import org.radarbase.jersey.exception.HttpNotFoundException

class MockProjectService(private val projects: List<String>) : ProjectService {
    override fun ensureProject(projectId: String) {
        if (projectId !in projects) {
            throw HttpNotFoundException("project_not_found", "Project $projectId not found.")
        }
    }
}
