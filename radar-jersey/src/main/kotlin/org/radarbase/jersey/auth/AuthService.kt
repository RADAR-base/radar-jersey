package org.radarbase.jersey.auth

import jakarta.inject.Provider
import jakarta.ws.rs.core.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.radarbase.auth.authorization.*
import org.radarbase.auth.token.DataRadarToken.Companion.copy
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.exception.HttpForbiddenException
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.exception.HttpUnauthorizedException
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.jersey.service.ProjectService
import org.slf4j.LoggerFactory

class AuthService(
    @Context private val oracle: AuthorizationOracle,
    @Context private val tokenProvider: Provider<RadarToken>,
    @Context private val projectService: ProjectService,
    @Context private val asyncService: AsyncCoroutineService,
) {
    private suspend fun requestScopedToken(): RadarToken = asyncService.runInRequestScope {
        try {
            tokenProvider.get().copy()
        } catch (ex: Throwable) {
            throw HttpForbiddenException("unauthorized", "User without authentication does not have permission.")
        }
    }

    /**
     * Check whether given [token] would have the [permission] scope in any of its roles. This doesn't
     * check whether [token] has access to a specific entity or global access.
     * @throws HttpForbiddenException if identity does not have scope
     */
    fun checkScope(permission: Permission, token: RadarToken, location: String? = null) {
        if (!oracle.hasScope(token, permission)) {
            throw forbiddenException(
                permission = permission,
                token = token,
                location = location,
            )
        }
        logAuthorized(permission, token, location)
    }

    /**
     * Check whether [token] has permission [permission], regarding given entity from [builder].
     * The permission is checked both for its
     * own entity scope and for the [EntityDetails.minimumEntityOrNull] entity scope.
     * @throws HttpForbiddenException if identity does not have permission
     */
    fun checkScopeAndPermission(
        permission: Permission,
        token: RadarToken,
        location: String? = null,
        builder: EntityDetails.() -> Unit,
    ): EntityDetails {
        if (!oracle.hasScope(token, permission)) {
            throw forbiddenException(
                permission = permission,
                token = token,
                location = location,
            )
        }
        val entity = EntityDetails().apply(builder)
        if (entity.minimumEntityOrNull() == null) {
            logAuthorized(permission, token, location)
        } else {
            checkPermissionBlocking(permission, entity, token, location, permission.entity)
        }
        return entity
    }

    suspend fun hasPermission(
        permission: Permission,
        entity: EntityDetails,
        token: RadarToken? = null,
    ) = oracle.hasPermission(token ?: requestScopedToken(), permission, entity)

    /**
     * Check whether [token] has permission [permission], regarding given [entity].
     * The permission is checked both for its
     * own entity scope and for the [EntityDetails.minimumEntityOrNull] entity scope.
     * @throws HttpForbiddenException if identity does not have permission
     */
    fun checkPermissionBlocking(
        permission: Permission,
        entity: EntityDetails,
        token: RadarToken? = null,
        location: String? = null,
        scope: Permission.Entity = permission.entity,
    ) = asyncService.runBlocking {
        checkPermission(permission, entity, token, location, scope)
    }

    suspend fun activeParticipantProject(token: RadarToken? = null): String? = (token ?: requestScopedToken()).roles
        .firstOrNull { it.role == RoleAuthority.PARTICIPANT }
        ?.referent

    /**
     * Check whether [token] has permission [permission], regarding given [entity].
     * The permission is checked both for its
     * own entity scope and for the [EntityDetails.minimumEntityOrNull] entity scope.
     * @throws HttpForbiddenException if identity does not have permission
     */
    suspend fun checkPermission(
        permission: Permission,
        entity: EntityDetails,
        token: RadarToken? = null,
        location: String? = null,
        scope: Permission.Entity = permission.entity,
    ) {
        entity.resolve()
        val actualToken = token ?: requestScopedToken()
        if (
            !oracle.hasPermission(
                actualToken,
                permission,
                entity,
                scope,
            )
        ) {
            throw forbiddenException(
                permission = permission,
                token = actualToken,
                location = location,
                entity,
            )
        }

        logAuthorized(
            permission = permission,
            token = actualToken,
            location = location,
            entity = entity,
        )
    }

    /**
     * Run [block] if [token] has permission [permission], regarding given [entity].
     * The permission check is run concurrently with the block. The block is cancelled if the
     * permission check fails. If the block costs significant resources, consider running
     * [checkPermission] before the block instead.
     * The permission is checked both for its
     * own entity scope and for the [EntityDetails.minimumEntityOrNull] entity scope.
     * @throws HttpForbiddenException if identity does not have permission
     */
    suspend inline fun <T> withPermission(
        permission: Permission,
        entity: EntityDetails,
        token: RadarToken? = null,
        location: String? = null,
        scope: Permission.Entity = permission.entity,
        crossinline block: suspend () -> T,
    ) = coroutineScope {
        val checkPermissionJob = async {
            checkPermission(permission, entity, token, location, scope)
        }
        val resultJob = async {
            block()
        }
        checkPermissionJob.await()
        resultJob.await()
    }

    private suspend fun EntityDetails.resolve() {
        val project = project
        val organization = organization
        if (project != null) {
            val org = projectService.projectOrganization(project)
            if (organization == null) {
                this.organization = org
            } else if (org != organization) {
                throw HttpNotFoundException(
                    "organization_not_found",
                    "Organization $organization not found for project $project.",
                )
            }
            val subject = subject
            if (subject != null) {
                projectService.ensureSubject(project, subject)
            }
        } else if (organization != null) {
            projectService.ensureOrganization(organization)
        }
    }

    suspend fun forbiddenException(
        permission: Permission,
        location: String? = null,
        entityDetails: EntityDetails? = null,
    ) = forbiddenException(permission, requestScopedToken(), location, entityDetails)

    fun forbiddenException(
        permission: Permission,
        token: RadarToken,
        location: String? = null,
        entityDetails: EntityDetails? = null,
    ): HttpForbiddenException {
        val message = logPermission(
            false,
            permission,
            token,
            location,
            entityDetails,
        )
        return HttpForbiddenException(
            "permission_mismatch",
            message,
            wwwAuthenticateHeader = HttpUnauthorizedException.wwwAuthenticateHeader(
                error = "insufficient_scope",
                errorDescription = message,
                scope = permission.toString(),
            ),
        )
    }

    suspend fun logAuthorized(
        permission: Permission,
        location: String? = null,
        entityDetails: EntityDetails? = null,
    ) = logAuthorized(permission, requestScopedToken(), location, entityDetails)

    fun logAuthorized(
        permission: Permission,
        token: RadarToken,
        location: String? = null,
        entity: EntityDetails? = null,
    ) = logPermission(true, permission, token, location, entity)

    private fun logPermission(
        isAuthorized: Boolean,
        permission: Permission,
        token: RadarToken,
        location: String? = null,
        entity: EntityDetails? = null,
    ): String {
        val message = if (!logger.isInfoEnabled && isAuthorized) {
            ""
        } else {
            buildString(140) {
                (location ?: findCallerMethod())?.let {
                    append(it)
                    append(" - ")
                }
                if (token.isClientCredentials) {
                    append(token.clientId)
                } else {
                    append('@')
                    append(token.username)
                }

                append(" - ")

                append(if (isAuthorized) "GRANTED " else "DENIED ")
                append(permission.scope())

                if (entity != null) {
                    append(' ')

                    buildList(6) {
                        entity.organization?.let { add("org: $it") }
                        entity.project?.let { add("proj: $it") }
                        entity.subject?.let { add("subj: $it") }
                        entity.source?.let { add("src: $it") }
                        entity.user?.let { add("@$it") }
                    }.joinTo(this, separator = ", ", prefix = "{", postfix = "}")
                }
            }
        }
        logger.info(message)
        return message
    }

    suspend fun referentsByScope(permission: Permission): AuthorityReferenceSet {
        return oracle.referentsByScope(requestScopedToken(), permission)
    }

    fun mayBeGranted(role: RoleAuthority, permission: Permission): Boolean = with(oracle) {
        role.mayBeGranted(permission)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthService::class.java)

        private val stackWalker = StackWalker
            .getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

        private fun findCallerMethod(): String? = stackWalker.walk { stream ->
            stream
                .skip(2) // this method and logPermission
                .filter { !it.isAuthMethod }
                .findFirst()
                .map { "${it.declaringClass.simpleName}.${it.methodName}" }
                .orElse(null)
        }

        private val StackWalker.StackFrame.isAuthMethod: Boolean
            get() = methodName.isAuthMethodName || declaringClass.isAuthClass

        private val String.isAuthMethodName: Boolean
            get() =
                startsWith("logAuthorized") ||
                startsWith("logPermission") ||
                startsWith("checkPermission") ||
                startsWith("invoke") ||
                startsWith("internal")

        private val Class<*>.isAuthClass: Boolean
            get() = this == AuthService::class.java ||
                isAnonymousClass ||
                isLocalClass ||
                name.contains("AuthService") ||
                simpleName == "ReflectionHelper"
    }
}
