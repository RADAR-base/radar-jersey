package org.radarbase.jersey.auth.jwt

import jakarta.ws.rs.core.Context
import org.radarbase.auth.authorization.AuthorizationOracle
import org.radarbase.auth.authorization.EntityRelationService
import org.radarbase.auth.authorization.MPAuthorizationOracle
import org.radarbase.jersey.service.ProjectService
import java.util.function.Supplier

class AuthorizationOracleFactory(
    @Context projectService: ProjectService,
) : Supplier<AuthorizationOracle> {
    private val relationService = object : EntityRelationService {
        override suspend fun findOrganizationOfProject(project: String): String {
            return projectService.projectOrganization(project)
        }
    }

    override fun get(): AuthorizationOracle = MPAuthorizationOracle(relationService)
}
