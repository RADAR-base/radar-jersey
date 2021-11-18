package org.radarbase.jersey.auth.disabled

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.auth.Auth
import java.util.*

/** Authorization that grants permission to all resources. */
class DisabledAuth(
        private val resourceName: String
) : Auth {
    override val defaultProject: String? = null
    override val token: RadarToken = EmptyToken()

    override fun getClaim(name: String): JsonNode = NullNode.instance

    override fun hasRole(projectId: String, role: String): Boolean = true

    inner class EmptyToken : RadarToken {
        override fun getRoles(): Map<String, List<String>> = emptyMap()

        override fun getAuthorities(): List<String> = emptyList()

        override fun getScopes(): List<String> = emptyList()

        override fun getSources(): List<String> = emptyList()

        override fun getGrantType(): String = "none"

        override fun getSubject(): String = "anonymous"

        override fun getUsername(): String = "anonymous"

        override fun getIssuedAt(): Date = Date()

        override fun getExpiresAt(): Date = Date(Long.MAX_VALUE)

        override fun getAudience(): List<String> = listOf(resourceName)

        override fun getToken(): String = ""

        override fun getIssuer(): String = "empty"

        override fun getType(): String = "none"

        override fun getClientId(): String = "none"

        override fun getClaimString(name: String?): String? = null

        override fun getClaimList(name: String?): List<String> = emptyList()

        override fun hasAuthority(authority: String?): Boolean = true

        override fun hasPermission(permission: Permission?): Boolean = true

        override fun hasPermissionOnProject(permission: Permission?, projectName: String?): Boolean = true

        override fun hasPermissionOnSubject(permission: Permission?, projectName: String?, subjectName: String?): Boolean = true

        override fun hasPermissionOnSource(permission: Permission?, projectName: String?, subjectName: String?, sourceId: String?): Boolean = true

        override fun isClientCredentials(): Boolean = false
    }
}
