/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth

import com.fasterxml.jackson.databind.JsonNode
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.exception.HttpBadRequestException
import org.radarbase.jersey.exception.HttpForbiddenException
import org.slf4j.LoggerFactory

interface Auth {
    /** Default project to apply operations to. */
    val defaultProject: String?

    val token: RadarToken

    /** ID of the OAuth client. */
    val clientId: String?
        get() = token.clientId

    /** User ID, if set in the authentication. This may be null if a client credentials grant type is used. */
    val userId: String?
        get() = token.subject?.takeUnless { it.isEmpty() }

    /**
     * Check whether the current authentication has given permissions on a subject in a project.
     *
     * @throws HttpBadRequestException if a parameter is null
     * @throws HttpForbiddenException if the current authentication does not authorize for the permission.
     */
    fun checkPermissionOnSubject(permission: Permission, projectId: String?, userId: String?, location: String? = null) {
        if (!token.hasPermissionOnSubject(permission,
                        projectId ?: throw HttpBadRequestException("project_id_missing", "Missing project ID in request"),
                        userId ?: throw HttpBadRequestException("user_id_missing", "Missing user ID in request")
                        )) {
            logPermission(false, permission, location, projectId, userId)
            throw HttpForbiddenException("permission_mismatch", "No permission '$permission' " +
                    "project $projectId with user $userId")
        }

        logPermission(true, permission, location, projectId, userId)
    }

    /**
     * Check whether the current authentication has given permissions on a project.
     *
     * @throws HttpBadRequestException if a parameter is null
     * @throws HttpForbiddenException if the current authentication does not authorize for the permission.
     */
    fun checkPermissionOnProject(permission: Permission, projectId: String?, location: String? = null) {
        if (!token.hasPermissionOnProject(permission,
                        projectId ?: throw HttpBadRequestException("project_id_missing", "Missing project ID in request")
                        )) {
            logPermission(false, permission, location, projectId)
            throw HttpForbiddenException("permission_mismatch", "No permission '$permission' for " +
                    "project $projectId")
        }
        logPermission(true, permission, location, projectId)
    }

    /**
     * Check whether the current authentication has given permissions.
     *
     * @throws HttpBadRequestException if a parameter is null
     * @throws HttpForbiddenException if the current authentication does not authorize for the permission.
     */
    fun checkPermissionOnSource(permission: Permission, projectId: String?, userId: String?, sourceId: String?, location: String? = null) {
        if (!token.hasPermissionOnSource(permission,
                        projectId ?: throw HttpBadRequestException("project_id_missing", "Missing project ID in request"),
                        userId ?: throw HttpBadRequestException("user_id_missing", "Missing user ID in request"),
                        sourceId ?: throw HttpBadRequestException("source_id_missing", "Missing source ID in request"))) {
            logPermission(false, permission, location, projectId, userId, sourceId)
            throw HttpForbiddenException("permission_mismatch", "No permission '$permission' for " +
                    "project $projectId with user $userId and source $sourceId")
        }
        logPermission(true, permission, location, projectId, userId, sourceId)
    }

    /**
     * Get a claim from the token used for this authentication.
     */
    fun getClaim(name: String): JsonNode

    /**
     * Whether the current authentication is for a user with a role in given project.
     */
    fun hasRole(projectId: String, role: String): Boolean

    fun logPermission(isAuthorized: Boolean, permission: Permission, location: String? = null, projectId: String? = null, userId: String? = null, sourceId: String? = null) {
        if (!logger.isInfoEnabled) {
            return
        }

         logger.info(StringBuilder(140).apply {
             (location ?: findCallerMethod())?.let {
                 append(it)
                 append(" - ")
             }
             if (token.isClientCredentials) {
                 append(clientId)
             } else {
                 append('@')
                 append(this@Auth.userId)
             }

             append(" - ")

             append(if (isAuthorized) "GRANTED " else "DENIED ")
             append(permission.scopeName())
             append(' ')

             ArrayList<String>(3).apply {
                 projectId?.let { add("project: $it") }
                 userId?.let { add("subject: $it") }
                 sourceId?.let { add("source: $it") }
             }.joinTo(this, separator = ", ", prefix = "{", postfix = "}")
         }.toString())
    }

    companion object {
        private val stackWalker = StackWalker
                .getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

        private fun findCallerMethod(): String? = stackWalker.walk { stream -> stream
                .skip(2) // this method and logPermission
                .filter { !it.isAuthMethod }
                .findFirst()
                .map { "${it.declaringClass.simpleName}.${it.methodName}" }
                .orElse(null)
        }

        private val logger = LoggerFactory.getLogger(Auth::class.java)

        private val StackWalker.StackFrame.isAuthMethod: Boolean
            get() = methodName.isAuthMethodName || declaringClass.isAuthClass

        private val String.isAuthMethodName: Boolean
            get() = startsWith("logPermission")
                || startsWith("checkPermission")
                || startsWith("invoke")

        private val Class<*>.isAuthClass: Boolean
            get() = isInstance(Auth::class.java)
                    || isAnonymousClass
                    || isLocalClass
                    || simpleName == "ReflectionHelper"
    }
}
